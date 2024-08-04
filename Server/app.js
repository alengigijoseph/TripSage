const express = require('express');
const mongoose = require('mongoose');
const traffroutes = require('./routes/route');
require('dotenv').config();

const app = express();
const port = 3000;

app.use('/route', traffroutes);

mongoose
  .connect(process.env.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  })
  .then(() => {
    console.log('Connected to MongoDB');
    console.log(process.env.JWT_SECRET)
    app.listen(port, () => {
      console.log(`Server is running on port ${port}`);
    });
  })
  .catch((error) => {
    console.error('Error connecting to MongoDB:', error);
  });
