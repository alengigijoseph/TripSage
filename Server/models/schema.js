const mongoose = require('mongoose');

const routeSchema = new mongoose.Schema({
  roadName: {
    type: String,
    trim: true,
    unique:true
  },
  time: {
    type: String
  },
  camera:[{
   camId:{
    type:String
   },
   time:{
    type:String
   }

  }],
  construction: [{
    location: [{
     lat:{
        type:String,
     },
     lng:{
        type: String
     }  
   }]
  }],
});

const Route = mongoose.model('Route', routeSchema);

module.exports = Route;