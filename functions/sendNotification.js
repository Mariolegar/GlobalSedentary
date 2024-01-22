const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();
exports.sendNotification = functions.https.onRequest((req, res) => {
  const registrationTokens = req.body.registrationTokens;
  const notificationTitle = req.body.notificationTitle;
  const notificationBody = req.body.notificationBody;

  const message = {
    token: registrationTokens,
    notification: {
      title: notificationTitle,
      body: notificationBody,
    },
  };

  admin.messaging().send(message)
      .then((response) => {
        console.log("Notification sent successfully:", response);
      })
      .catch((error) => {
        console.log(error.stack);
        res.status(500).send("error");
      });
  return Promise.all([
  ]).then(() => {
    res.status(200).send("ok");
  }).catch((err) => {
    console.log(err.stack);
    res.status(500).send("error");
  });
});
