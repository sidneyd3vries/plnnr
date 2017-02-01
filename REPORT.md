# Plnnr

## Overview

Plnnr is an app that makes it easy to plan a night out with friends. In this app you can create groups, every group has a pinboard where users can post places they want to go to. Other users can vote on these items and this way you can see what plan is most popular. Places are found similar to how you search in Google Maps, but here you can search for multiple places in one time, either places in a certain category, or places matching a text search. 

![](mooi_plaatje.png) 
## Technical design

### High level overview

The first time you open the app you get to the sign in activity. This activity communicates with Google and Firebase for the log in. If the user is succesful the group activity is shown. Here the groups the user is part of are loaded from firebase. When the user selects a group the pinboard activity is shown. Here the data matching the groups identifier is loaded from Firebase and shown. Here dialogs to leave a group, add users or change the group name. From here the user can navigate to the map activity where the Google places API is used to get data, and firebase is used to save the data. See the flowchart for a global visualization of activities and how they work together.


### Details

## Challenges
The first challenge was the original idea of a chat room, however this was trying to reinvent the wheel (eg. Whatsapp or Telegram) so I decided to change the chat into a pinboard. This made saving and receiving the data easier. Another thing I had problems with was getting all the Google services working, but it turned out I forgot to turn the API's on, so that was solved quickly. A problem I had with firebase was that you can't request things such as a name or email from other users, only yourself. This was a problem with showing the other members of your group. I solved this by writing a method that saved the user's id and the user's email in separate 'column'. This way I could get the email of other users with their id. Another problem is something that is out of my power, and that is a bug that a user is signed in with Google, but not with Firebase so the app would get stuck in an invite loop.
## Decisions

## Screenshots

![](mooi_plaatje.png)

![](mooi_plaatje.png)

![](mooi_plaatje.png)

![](mooi_plaatje.png)

![](mooi_plaatje.png)

![](mooi_plaatje.png)

![](mooi_plaatje.png)

![](mooi_plaatje.png)

