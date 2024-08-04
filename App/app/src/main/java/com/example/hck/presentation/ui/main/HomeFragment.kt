package com.example.hck.presentation.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hck.R
import com.example.hck.databinding.FragmentHomeBinding
import com.example.hck.presentation.ui.map.FetchRouteActivity
import com.example.hck.presentation.ui.search.SearchActivity
import com.example.hck.presentation.ui.search.SearchTwoActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var switch: ImageButton
    private lateinit var googleMap: GoogleMap
    private val binding get() = _binding!!
    lateinit var to: TextView
    lateinit var from: TextView
    lateinit var search: Button

    /*var myLat:String=""
    var myLng:String=""*/
    var fromLat: String=""
    var fromLng: String=""
    var toLat: String=""
    var toLng: String=""

    lateinit var viewModel: MainViewModel

   private val navigationLocationProvider = NavigationLocationProvider()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1000) {
            if (resultCode === Activity.RESULT_OK) {
                val receivedData: String? = data?.getStringExtra("from");
                fromLat = data?.getStringExtra("lat")!!
                fromLng = data.getStringExtra("lng")!!
                receivedData?.let { viewModel.updateFromValue(it) }
            }
        }
        if (requestCode === 2000) {
            if (resultCode === Activity.RESULT_OK) {
                val receivedData: String? = data?.getStringExtra("to");
                toLat = data?.getStringExtra("lat")!!
                toLng = data.getStringExtra("lng")!!
                receivedData?.let { viewModel.updateToValue(it) }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        to = binding.to2
        from = binding.from
        search = binding.searchplaces
        switch = binding.switchbutton

        viewModel.from.observe(viewLifecycleOwner) {
            from.text = it
        }
        viewModel.to.observe(viewLifecycleOwner) {
            to.text = it
        }

        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        }

        /*binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }*/

        from.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(intent,1000)
        }

        to.setOnClickListener {
            val intent = Intent(requireContext(), SearchTwoActivity::class.java)
            startActivityForResult(intent,2000)
        }

        search.setOnClickListener{

            if(fromLat.isEmpty() && fromLng.isEmpty()){
                fromLat= originPoint?.latitude().toString()
                fromLng= originPoint?.longitude().toString()
            }
            if(toLat.isEmpty() && toLng.isEmpty() && fromLat.isEmpty() && fromLng.isEmpty()){
                Toast.makeText(requireContext(),"Places cannot be empty",Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(requireContext(), FetchRouteActivity::class.java)
                intent.putExtra("fromLat", fromLat);
                intent.putExtra("fromLng", fromLng);
                intent.putExtra("toLat", toLat);
                intent.putExtra("toLng", toLng);
                Log.d("lat",fromLat)
                Log.d("latt",fromLng)
                Log.d("lattt",toLat)
                Log.d("latttt",toLng)
                startActivity(intent)

            }

        }

        switch.setOnClickListener{
            val text1 = from.text.toString()
            val text2 = to.text.toString()

            if (text1.isEmpty()) {
                Toast.makeText(context, "Cannot Switch for Current Location", Toast.LENGTH_SHORT).show()
            }
            else if (text2.isEmpty()) {
                Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else {
                val tempText = from.text.toString()
                from.setText(to.text.toString())
                to.setText(tempText)
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.uiSettings.isZoomControlsEnabled = true
            checkLocationPermission()
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getLastLocation()
                enableMyLocation()
            }
        }
        else if (requestCode == LOCATION_PERMISSION_TWO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_TWO_REQUEST_CODE = 101
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        fromLat = it.latitude.toString()
                        fromLng = it.longitude.toString()
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("You are here")

                        )?.showInfoWindow()
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
        }
    }
}