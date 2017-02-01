# Plnnr

## Overview

Plnnr is an app that makes it easy to plan a night out with friends. In this app you can create groups, every group has a pinboard where users can post places they want to go to. Other users can vote on these items and this way you can see what plan is most popular. Places are found similar to how you search in Google Maps, but here you can search for multiple places in one time, either places in a certain category, or places matching a text search. 

![](mooi_plaatje.png)

## Technical design

### High level overview

The first time you open the app you get to the sign in activity. This activity communicates with Google and Firebase for the log in. If the user is succesful the group activity is shown. Here the groups the user is part of are loaded from firebase. When the user selects a group the pinboard activity is shown. Here the data matching the groups identifier is loaded from Firebase and shown. Here dialogs to leave a group, add users or change the group name. From here the user can navigate to the map activity where the Google places API is used to get data, and firebase is used to save the data. See the flowchart for a global visualization of activities and how they work together.


### Details

## Challenges

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

