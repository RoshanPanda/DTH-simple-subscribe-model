# DTH-simple-subscribe-model
Simple DTH subscribe model that can be enhanced to add/explore more things

This expose an api to subscribe someone to the service and accepts JSON payload as request.

To run the project use below command

``sbt run``

To test the test cases use below command

``sbt test``

Once app runs iut exposes below Uris

``http://localhost:8080/subscribe`` to subscribe to the service.

``http://localhost:8080/subscribe/details`` to get the subscription details using  the provided userId.

``http://localhost:8080/subscribe/addChannel`` if you want to add ay additional channel using UserId.

### Sample Example:-
1. Valid JSON request and response to subscribe someone.

**Valid packages** :- **basic, entertainment, sportsPlus** and each package has default
channel list that user will get from the subscription.

valid plan :- **Monthly, Biannual, Annual** 
mandatory fileds :- **email,fullame,phNo**

```json
{
"additionalChannel": ["ZEE"],
"email": "roshan.raj.panda@gmail.com",
"fullName": "Roshan Raj Panda",
"packageName": ["basic"],
"phNo": "0123456789",
"plan": "Monthly"
}
```

Response:-
```json
{
    "channels": [
        "DDNational",
        "DDSports",
        "DDNews",
        "ZEE"
    ],
    "packageName": [
        "basic"
    ],
    "plan": "Monthly",
    "userId": "twTE2DJVtr"
}
```
On successful subscription you will get an Uid and based on Uid 
someone can check the subscription details and also add additional chanel if required.

2. Valid request abd response to check details

```json
{"uid":"twTE2DJVtr"}
```

Response:-
```json
{
    "channels": [
        "DDNational",
        "DDSports",
        "DDNews",
        "ZEE"
    ],
    "packageName": [
        "basic"
    ],
    "plan": "Monthly",
    "userId": "twTE2DJVtr"
}
```
3. Valid request and response to add new channel

```json
{"name":["BTSports"],"uid":"twTE2DJVtr"}
```
Response:-

```json
{
    "channels": [
        "DDNational",
        "DDSports",
        "DDNews",
        "DDNational",
        "BTSports"
    ],
    "packageName": [
        "basic"
    ],
    "plan": "Monthly",
    "userId": "twTE2DJVtr"
}
```

### Validation:-
Someone should subscribe to valid packages i.e **Monthly, Biannual, Annual**
```json
{
"additionalChannel": ["ZEE"],
"email": "roshan.raj.panda@gmail.com",
"fullName": "Roshan Raj Panda",
"packageName": ["basic"],
"phNo": "0123456789",
"plan": "Monthlyyyyy"
}
```
Response:-
```json
{
    "message": "Chain(InvalidError(Select valid plan from Monthly, Biannual, Yearly))",
    "success": false
}
```
#### TODO:- 
1. Use phone Number as unique identifier
2. Add more support to expiry and renew plans
3. add validation on chanel level(some one only select valid channel name to subscribe)