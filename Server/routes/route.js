const express = require('express');
const Route = require('../models/schema'); 
const router = express.Router();


router.use(express.json());

router.post('/bestroute', async (req, res) => {
  try {
    const { places } = req.body; 

    if (!places || !Array.isArray(places)) {
      return res.status(400).json({ message: 'Invalid request body' });
    }

    const responseData = [];

    for (const route of places) {
      const { roadnum, rdnames } = route;
      const roadsForRoute = [];

      for (const roadName of rdnames) {
        const routeData = await Route.findOne({ roadName });

        if (routeData) {
          const road = {
            name: routeData.roadName,
            time: routeData.time,
            construction: routeData.construction.map(coord => ({
              lat: coord.lat,
              lng: coord.lng
            }))
          };
          roadsForRoute.push(road);
        }
      }

      responseData.push({
        route: roadnum,
        roads: roadsForRoute
      });
    }

    res.status(200).json({ data: responseData });
  } catch (error) {
    console.error('Error searching routes:', error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

router.post('/savedata', async (req, res) => {
  try {
    const { roadName, cameraId, timeEst} = req.body;

    if (!roadName || !cameraId || !timeEst) {
      return res.status(400).json({ message: 'Missing required fields in the request body' });
    }

    let existingRoute = await Route.findOne({ roadName });

    if (!existingRoute) {
      
      existingRoute = new Route({
        roadName,
        camera: [{ camId: cameraId, time: timeEst }]
      });
    } else {
      
      const cameraIndex = existingRoute.camera.findIndex(cam => cam.camId === cameraId);
      if (cameraIndex === -1) {
        existingRoute.camera.push({ camId: cameraId, time: timeEst });
      } else {
        existingRoute.camera[cameraIndex].time = timeEst;
      }
    }

    await existingRoute.save();

    res.status(201).json({ message: 'Route data saved successfully' });
  } catch (error) {
    console.error('Error saving route data:', error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

module.exports = router;

  module.exports = router;