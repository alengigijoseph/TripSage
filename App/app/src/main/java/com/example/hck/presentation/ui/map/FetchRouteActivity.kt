package com.example.hck.presentation.ui.map

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hck.R
import com.example.hck.data.requests.PlacesRequest
import com.example.hck.data.requests.Rd
import com.example.hck.databinding.ActivityFetchRouteBinding
import com.example.hck.data.resp.map.Routes
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.annotation.RequiresExtension
import com.example.hck.domain.model.Temp
import com.example.hck.domain.repo.MapsRepository
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class FetchRouteActivity : AppCompatActivity() {

    @Inject lateinit var mapsRepository: MapsRepository

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    private val mapboxReplayer = MapboxReplayer()

    //private val viewModel: MapViewModel by viewModels()
    private val viewModel: MapViewModel by viewModels {
        MapViewModel.provideFactory(mapsRepository, this)
    }

    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    private lateinit var binding: ActivityFetchRouteBinding

    private lateinit var navigationCamera: NavigationCamera

    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private lateinit var maneuverApi: MapboxManeuverApi

    private lateinit var tripProgressApi: MapboxTripProgressApi

    private lateinit var routeLineApi: MapboxRouteLineApi

    private lateinit var routeLineView: MapboxRouteLineView

    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    private lateinit var routeArrowView: MapboxRouteArrowView

    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    private lateinit var speechApi: MapboxSpeechApi

    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    private val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = binding.mapView.getMapboxMap().getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this@FetchRouteActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                binding.mapView.getMapboxMap().getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = binding.mapView.getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    var fromLat: String=""
    var fromLng: String=""
    var toLat: String=""
    var toLng: String=""

    var placesList: MutableList<List<String>> =  mutableListOf()

    lateinit var dialog: Dialog
    lateinit var fetchtxt: TextView
    lateinit var shorttxt: TextView
    lateinit var traffictxt: TextView
    lateinit var fetchpb:ProgressBar
    lateinit var shortpb:ProgressBar
    lateinit var trafficpb:ProgressBar
    lateinit var fetchimg:ImageView
    lateinit var shortimg:ImageView
    lateinit var trafficimg:ImageView


    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
                // start the trip session to being receiving location updates in free drive
                // and later when a route is set also receiving route progress updates
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            }
        },
        onInitialize = {
            findRoute()
        }
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFetchRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //viewModel = ViewModelProvider(this@FetchRouteActivity).get(MapViewModel::class.java)

        fromLat = intent.getStringExtra("fromLat")!!
        fromLng = intent.getStringExtra("fromLng")!!
        toLat = intent.getStringExtra("toLat")!!
        toLng = intent.getStringExtra("toLng")!!
        Log.d("coord",fromLat+" "+fromLng+" "+toLat+" "+toLng)

        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this@FetchRouteActivity)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

        viewportDataSource = MapboxNavigationViewportDataSource(binding.mapView.getMapboxMap())
        navigationCamera = NavigationCamera(
            binding.mapView.getMapboxMap(),
            binding.mapView.camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )

        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label-navigation")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        binding.mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) {
            //findRoute()
        }

        // initialize view interactions
        binding.stop.setOnClickListener {
            clearRouteAndStopNavigation()

        }
        binding.recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        binding.soundButton.unmute()

        dialog = Dialog(this)
        dialog.setContentView(R.layout.fetch_dialog)
        dialog.setCanceledOnTouchOutside(false)

        fetchtxt= dialog.findViewById(R.id.fetchtext)
        shorttxt= dialog.findViewById(R.id.shorttext)
        traffictxt= dialog.findViewById(R.id.traffictext)

        fetchimg= dialog.findViewById(R.id.fetchimg)
        shortimg= dialog.findViewById(R.id.shortimg)
        trafficimg= dialog.findViewById(R.id.trafficimg)

        fetchpb= dialog.findViewById(R.id.fetchpb)
        shortpb= dialog.findViewById(R.id.shortpb)
        trafficpb= dialog.findViewById(R.id.trafficpb)

        dialog.show()

    }

    /*private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )

        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

    }*/

    private fun findRoute() {
        //val originLocation = navigationLocationProvider.lastLocation
        /*val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return*/

            val originPoint = Point.fromLngLat(fromLng.toDouble(),fromLat.toDouble())
            val destination= Point.fromLngLat(toLng.toDouble(),toLat.toDouble())

            mapboxNavigation.requestRoutes(
                RouteOptions.builder()
                    .applyDefaultNavigationOptions()
                    .applyLanguageAndVoiceUnitOptions(this)
                    .coordinatesList(listOf(originPoint, destination))
                    .bearingsList(
                        listOf(
                            Bearing.builder()
                                //.angle(originLocation.bearing.toDouble())
                                .degrees(45.0)
                                .build(),
                            null
                        )
                    )
                    .layersList(listOf(mapboxNavigation.getZLevel(), null))
                    .alternatives(true)
                    .build(),
                object : NavigationRouterCallback {
                    override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                        // no impl
                    }

                    override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                        // no impl
                    }

                    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: RouterOrigin
                    ) {
                        val currentRoutes = mapboxNavigation.getNavigationRoutes()
                        Log.d("hehee",currentRoutes.toString())

                        fetchpb.visibility  = GONE
                        fetchimg.visibility = VISIBLE
                        shorttxt.visibility= VISIBLE
                        shortpb.visibility= VISIBLE
                        //setRouteAndStartNavigation(routes)
                        val gson = GsonBuilder().setPrettyPrinting().create()

                        val routess = routes.map {gson.fromJson(it.directionsResponse.toJson(),
                            Routes::class.java) }

                        val roadsMap = mutableMapOf<Int, MutableList<String>>()

                        setRouteAndStartNavigation(routes)
                        dialog.dismiss()
                        /*routess.forEach { routesItem ->
                            var roadnum = 0
                            routesItem.routes.forEach { route ->
                                val places = route.legs.flatMap { it.summary.split(", ") }
                                if (!roadsMap.containsKey(roadnum)) {
                                    roadsMap[roadnum] = mutableListOf()
                                }
                                roadsMap[roadnum]?.addAll(places)
                                roadnum++
                            }
                        }

                        val roads = roadsMap.map { (roadnum, names) ->
                            Rd(roadnum = roadnum, rdnames = names.distinct())
                        }
                        val req = PlacesRequest(roads)
                        Log.d("root",req.toString())
                        val tmp = mutableListOf<Temp>()*/
                        /*lifecycleScope.launch {
                            try {
                                val resp = viewModel.bestRoute(req)
                                Log.d("roott",resp.toString())
                                val data = resp.data
                                data.forEach { result ->
                                    var time = 0.00
                                    val selectedRoute = result.roadnum
                                    result.roads.forEach { road ->
                                        time += road.time.toDouble()
                                    }
                                    val temp = Temp(
                                        selectedroute = selectedRoute,
                                        timetaken = time
                                    )
                                    Log.d("tyuuu",temp.toString())
                                    tmp.add(temp) // Add data to the mutable list
                                }

                                var maxIndex = -1
                                var maxTimeTaken = 0.00

                                tmp.forEachIndexed { index, temp ->
                                    if (temp.timetaken > maxTimeTaken) {
                                        maxTimeTaken = temp.timetaken
                                        maxIndex = index
                                    }
                                }

                                if (maxIndex != -1) {
                                    val zeroIndex = routes[0]
                                    val elementToMove = routes[maxIndex]
                                    val reorderedRoutes = routes.toMutableList()
                                    reorderedRoutes.removeAt(maxIndex)
                                    reorderedRoutes.removeAt(0)
                                    reorderedRoutes.add(0, elementToMove)
                                    reorderedRoutes.add(maxIndex, zeroIndex)
                                    val finalRoutes = reorderedRoutes.toList()

                                    setRouteAndStartNavigation(finalRoutes)
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(this@FetchRouteActivity,"Failed to fetch routes",Toast.LENGTH_SHORT).show()
                                }

                                *//*shortpb.visibility= GONE
                                shortimg.visibility= VISIBLE
                                traffictxt.visibility= VISIBLE
                                trafficpb.visibility= VISIBLE

                                val json = routes.map {
                                    gson.toJson(
                                        JsonParser.parseString(it.directionsRoute.toJson())
                                    )
                                }

                                val zeroindex= routes[0]
                                val elementToMove = routes[maxIndex]
                                val reorderedRoutes = routes.toMutableList()
                                reorderedRoutes.removeAt(maxIndex)
                                reorderedRoutes.removeAt(0)
                                reorderedRoutes.add(0, elementToMove)
                                reorderedRoutes.add(maxIndex,zeroindex)
                                reorderedRoutes.toList()


                                setRouteAndStartNavigation(reorderedRoutes)
                                *//*
                            } catch (e: HttpException) {
                            } catch (e: Exception) {
                            }
                        }
*/


                    }
                }
            )

    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setNavigationRoutes(listOf())

        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        binding.soundButton.visibility = View.INVISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE

        onBackPressed()
    }

    private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setNavigationRoutes(routes = routes)

        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }
    /*private fun fetchARoute() {
        val networkClient = NetworkClient()
        val base_url = "https://api.mapbox.com/directions/v5/mapbox/driving"
        val token = getString(R.string.mapbox_access_token)
        val url = "$base_url/$fromLng%2C$fromLat%3B$toLng%2C$toLat?alternatives=true&geometries=polyline6&language=en&overview=full&steps=true&access_token=$token"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = networkClient.run(url)
                val routes = Gson().fromJson(response, Routes::class.java)
                routes.routes.forEach { route ->
                    route.legs.forEach { leg ->
                        val summary = leg.summary
                        val places = summary.split(", ")
                    }
                }
                withContext(Dispatchers.Main) {

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }*/

}