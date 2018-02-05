SPCORE README
=============

Logging in
----------

For logging in to the app please use my (Budi's) SPice ID:
1626175
c*w&lP#EMS0!y!q3IxYRV1Veql0H2n0

Do expect that the user initialization process (which caches the timetable on the backend, and also locally on the phone) will take around 1-2mins as the school's timetable endpoint is slow.


Running the server locally
--------------------------

We currently have hosted the server on the cloud so that it's easier for you to test the app.
However, if for any reason the app crashes or hangs on splashcreen, most likely the server is down.


If that is the case, you can run the following.
--------
Terminal -> java -jar spcoreServer.jar
MySQL -> create a schema called 'spcore', then scaffold using the db.sql provided

Make sure that 
 - mysql credentials are user = root & password = 12345
 - phone is connected to the same wifi AP
 - using jdk8 & kotlin 1.10.+ or higher to build
 - using Android studio 3.+

Then, get your machine's ipv4 address and change line 13 of Backend.kt to

	internal const val BACKEND_URL: String = "http://<your ipv4 here>:8080"

If there are still issues, please contact budisyahiddin@gmail.com / euwbah@gmail.com
