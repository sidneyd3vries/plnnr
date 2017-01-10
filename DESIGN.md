# Design document

## Sidney de Vries

### Classes

#### Log-in class
Class where all the signing in is done. Will be using the google signup methods, also firebase for storing the list (however this list could also be saved using shared preferences). In the first version of the app this activity is linked with the list activity, however later on this activity will link to the chat overview activity. 

#### List class
This activity shows an overview of plans that are made. This list is stored using firebase. From this ativity it is possible to go to the map activity, where the user can create a plan to add to the list.

#### Map class
Using the Google Maps Places API it is possible to get placeID's from selected places and store them. Using the getplacebyId function from the api it is possible to recieve information you want from the place. The eventual plan made here is saved on firebase so it can be accessed from the List Class. The user can look for categories of places, or look for a certain name. The list of all places that match the criteria are saved and later used to create the final plan that will be added to the plan list. 

#### Helper class(es)
The Helper Class will contain all methods that are used multiple times, so that you wont have the same method defined in multiple activities. Could be divided in multiple classes if the methods are categorizable. 

### API class
All code that is mostly copy pasted from the API documentation will be put here. This is done to keep code clear and separate own written code and copy pasted code. Could also be divided in multiple classes if the methods are categorizable. 

#### Chat overview class
If implemented this class will get data from Firebase to show the chats the logged in user is part of. Here the user can make new chats and invite people. This activity links to the single chat class.

#### Single chat class
Here all data from the linked firebase subdatabase is displayed in the form of a chat. This activity links to the list activity, where the list linked to the chat will be shown.

### API's

#### Google Maps Places API
As said earlier the app is going to use the Places API, where the data received can be json or XML. I will probablu chose json but this could change if the XML is more useful. This search query contains the location, the range, the type and or name/keywords.

#### Firebase
