[SPcore Logo](/app/src/main/res/drawable/logo_black.png)

## Backend info

##### GET **getSchedule**

###### URL inline params:
- jwtToken: String
    - JWT token provided upon session log in
- from: epoch ms timestamp
    - The date to inclusively start giving schedule objects from
- to: epoch ms timestamp
    - The date to inclusively start giving schedule objects from

###### Response

```json
{
  "schedule": [
    // Example of lesson schedule item type
    {
      "type": "lesson",
      
      // id should be a number which is the result of hashing the
      // moduleCode, location, and start using Objects.hash(varargs objs...)
      // Doing so will allow different users in the same lesson to receive
      // a logically identical object, and will facillitate a possible
      // future addition to see other users in the same lesson
      "id": 1234, // 32-bit Integer
      "moduleCode": "ST1234",
      "moduleName": "MAPP",
      "location": "T1234",
      "start": 123456789, // 64-bit epoch timestamp in millisec
      "end": 123456789 // 64-bit epoch timestamp in millisec
    },
    // Example of event schedule item type
    {
      "type": "event",
      
      // id in the case of events is simply just an incrementing number
      // that doesn't repeat
      "id": 1234, // 32-bit Integer
      "title": "Event title",
      "description": "Event description",
      "location": "Some random location",
      "start": 123456789, // 64-bit epoch timestamp in millisec
      "end": 123456789, // 64-bit epoch timestamp in millisec
      // deletedInvite refers to the people who have completely 
      // deleted the event from their calendar
      "deletedInvite": [[Person](#person)],
      // Those who have responded with "Going"
      "going": [[Person](#person)],
      // Those who haven't responded
      "unconfirmed": [[Person](#person)],
      // Those who responded with not going
      "notGoing": [[Person](#person)]
    }
  ]
}
```

#### JSON Datatypes

##### Person

```json
// TODO
```