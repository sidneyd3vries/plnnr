# Day 2
Made a start in the design document, made sketches and flowchart for interaction between activities and API's

# Day 3
Pitch gave new ideas for app; replace the chat system with just the pinboard/list of plans. The chat is not a critical part of the app so it can be easily replaced. Remade flowcharts and sketches to match new idea

# Day 4
Spent most of the day trying to update google play services and getting google maps API working. Took way more time than I wanted. Finished the navigation and most basic functions of the app for the demo.

# Day 5
Brainstorming about the structure of my database, might change later because the demo gave ideas to change the pinboard system.

# Day 6
Found out part of the api I wanted to use is deprecated, now looking for/ thinking about alternatives. Fixed bug that froze app on login. Fixed maps fragment not showing correct location on load. Api querying works, should now be able to add all search results to the map

# Day 7
API key stopped working so I had to spend 2 hours fixing it. Linked views to make a query. Data is being retrieved but due the 2 hours wasted the data is not yet processed and out in to the map. 

# Day 8
Changed api after finding out manually asking for data was too much work. New query gives more information so I can do more. Started working on sending places to firebase, but the change in query for the api made this stop working so I am now rewriting it. Also started with overflow menu's and a small start with the design of the app.

# Day 9
Got basic version of database working, groups are not implemented yet but I can send plans to the database and populate listviews with data from the database. 

# Day 10
Didn't feel very well so I did not work on the app.

# Day 11
Restructured database and rewritten the code I already had. Had a lot of trouble with the dialog of adding people to a group. Adding edittexts with dynamic id's posed a lot of problems, but I got it working after more time than I wanted.

# Day 12
Groups are almost working, had some problems today with a leakedWindow error and also had to fix an infinite loop where an user would not be logged in and an activity would keep reloading. Code for the lists per group is almost done and making groups works for the biggest part.

# Day 13
Groups are working 95%. You can not leave a group yet but that is something that is done quickly. Also you can not see the other users in your group, and im planning on fixing that tommorrow.

# Day 14
Finally got working that you can see who are in your group, this took way more work then i expected it to. This had mainly to do with the way I structured my database. But I got it working. I also got working that you can delete items from the lists, you can leave groups and I did some small ui tweaks such as overflow menu's and floating action buttons. THe biggest thing to do now is more input validation and some extra's such as another search option on the map activity, and the possibility to find a list item on the map immediately.

# Day 15
Started working on tabs for the pinboard activity, and started implementing the current location of the user to show in the map activities. Got both of these working in the weekend. For the tabs I used a singleton to communicate the group identifier between them, because after lots of work I couldnt get this working the "normal" way.

# Day 16
Started restructuring some code and fixed some bugs with sign out/sign in. Also implemented asking for location permission when the user is signed in, instead of when the map opens. This solved a bug that made the app crash. 

# Day 17
More code restructuring and did some changes to the UI of the app. I changed the login screen and the background of the app. Also made the map activity nicer. 

# Day 18
Working on report and more code restructuring

# Day 19
More refactoring working with the report from bettercodehub. Minor problems with code moved to other classes, but nothing major.
