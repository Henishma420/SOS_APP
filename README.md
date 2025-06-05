# SOS_APP
This all-in-one emergency app helps users stay safe by sending real-time SOS alerts to trusted contacts during accidents or threats. Shows the live location of the person as well as the nearby hospital with audio assistance.

*App Overview – Emergency Safety & Navigation App*
This innovative mobile application is a multi-functional safety companion designed to assist users during emergencies and help them access critical services quickly and efficiently. The app smartly combines three major functionalities into a single user-friendly interface.

🆘 SOS Alert System
The primary module of the app functions as an SOS alert system to notify trusted contacts during emergencies. It offers two methods of triggering emergency alerts:
1. Manual SOS Trigger
Users can input and save the name and phone number of an emergency contact.
When needed, the user can press the "Send Emergency Alert" button to instantly send an SOS to the saved contact.

2. Automatic SOS Detection via Gyroscope
The app utilizes the device's gyroscope sensor to detect abnormal or intense shaking (e.g., during accidents or physical danger).
If such movement is detected, a countdown timer (starting from 60 seconds) is initiated with vibration alerts.
If the user does not cancel the countdown, the SOS message is automatically sent to the saved contact.

📍 Real-Time Location & Traffic Mapping
The app features an integrated map view displaying the user’s current location along with real-time traffic data:
Areas with heavy traffic congestion are marked in red, helping users avoid crowded or unsafe zones.
The location services stay active to ensure accuracy and provide situational awareness.

🏥 Nearby Emergency Services (Hospitals)
This module allows users to quickly find nearby hospitals in emergency scenarios:
By tapping the "Search Nearby Places" button, users receive a list of hospitals closest to their current location, along with the distance in meters.
Additionally, users can write a the letter H on the screen, which triggers an audio assistant to speak aloud the names and locations of nearby hospitals – a feature designed for users who may be visually impaired or unable to read the screen during a crisis.


Key Highlights

🔔 Two-step SOS (manual and automatic gyroscope-based)
🧭 Live location with red-marked traffic indicators
🏥 Instant access to nearby hospitals and voice guidance
🎯 Designed with accessibility, speed, and safety in mind

*Real-Time SOS Management Using Firebase*
This application integrates a powerful real-time emergency response system using Firebase Realtime Database to track and manage SOS alerts effectively. The backend is designed to be responsive, secure, and scalable — ideal for real-time emergencies where every second counts.


🆘 SOS Request Logging with Firebase

When the user taps the "Send Emergency Alert" button in the app, an SOS request is automatically created and pushed to the Firebase Realtime Database.

📡 How It Works:
Each SOS request is stored under a randomly generated unique request ID, ensuring no two SOS alerts conflict or overlap.
The request entry in Firebase includes the following data:
Latitude – The current GPS latitude of the user.
Longitude – The current GPS longitude of the user.
Request ID – A unique string identifier generated automatically.
Status – Indicates the current state of the SOS (e.g., "active", "cancelled", or "resolved").


This structure allows emergency responders, admins, or concerned contacts to monitor live SOS activity through the Firebase dashboard or via a connected admin app/interface.

🧠 Why Firebase?
Firebase Realtime Database was chosen for its:
⚡ Instant data syncing between the user and the database
☁ Cloud-based storage – accessible from any device
🔐 Secure and scalable data handling
🔁 Real-time update mechanism, ensuring immediate visibility of SOS alerts

Whenever an SOS is triggered, any connected system or admin panel listening to the Firebase database will be notified in real-time, enabling instant action or response.
