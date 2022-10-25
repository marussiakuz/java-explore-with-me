# <h1 align="center">Explore With Me</h1>

<br/>

## The application is a platform where you can offer an event (from an exhibition to going to the cinema or to a concert) and gather a company to participate in it.

### It's represented by two services: 
`the main service` is responsible for all business logic, `the second service` is responsible for collecting event viewing statistics and allows you to make various selections to analyze the operation of the application.

<br/>

**The public API:**
<br/>:white_check_mark: Provides event search and filtering capabilities.
<br/>:white_check_mark: Sorting of the list of events is possible either by the number of views, information about which is requested in the statistics service, or by the dates of events.
<br/>:white_check_mark: When viewing the list of events, only brief information about the events is returned.
<br/>:white_check_mark: The ability to view detailed information about a specific event is configured.
<br/>:white_check_mark: Each event belongs to one of the categories fixed in the application.
<br/>:white_check_mark: The ability to receive all available categories and collections of events is configured (such collections are made by resource administrators).
<br/>:white_check_mark: Each public request for a list of events or full information about the event is recorded by the statistics service.

<br/>

**API for authorized users:**
<br/>:white_check_mark: The closed part of the API implements the capabilities of registered users of the product.
<br/>:white_check_mark: Authorized users have the ability to add new events to the application, edit them and view them after they are added.
<br/>:white_check_mark: The submission of applications for participation in the events of interest is set up.
<br/>:white_check_mark: The creator of the event has the opportunity to confirm or reject applications for participation in the event submitted by other users of the service
<br/>:white_check_mark: If the event has not been moderated by the administrator, the initiator of the event can view the comment left by the administrator with a detailed description of the necessary corrections 
<br/>:white_check_mark: After making corrections in accordance with the comment of the administrator, the event is re-moderated

<br/>

**API for the administrator:**
<br/>:white_check_mark: The administrative part of the API will provide the ability to configure and maintain the service.
<br/>:white_check_mark: It's configured to add, modify, and delete categories for events.
<br/>:white_check_mark: Added the ability to add, delete and pin a selection of events on the main page.
<br/>:white_check_mark: Moderation of events posted by users is set - publication or rejection.
<br/>:white_check_mark: If the event is rejected as a result of moderation, the administrator has the opportunity to leave a comment with a detailed description of the required changes.
<br/>:white_check_mark: An event that is being re-moderated can be successfully published and all previously opened comments are closed, or the administrator can reject it again, accompanied by a comment
<br/>:white_check_mark: User management is configured — add, view and delete.

</br>

**Service functionality:**
<br/>:white_check_mark: contains a record of information that a request to the API endpoint was processed - date and time, user IP address, url;
<br/>:white_check_mark: providing statistics for selected dates for the selected endpoint.

</br>

    1. API of the main service: [main API](https://github.com/marussiakuz/java-explore-with-me/blob/main/ewm-main-service-spec.json)
    2. API of the statistics collection service: [stat API](https://github.com/marussiakuz/java-explore-with-me/blob/main/ewm-stats-service-spec.json)
    

<div>
    <h1 align="center">
    Architecture diagram: 
    </h1>
<div/>
<br/>
<div>
  <p align="center">
    <img width="180" height="1000" alt="ewm architecture diagram" src="https://user-images.githubusercontent.com/96682553/191831459-413f88c2-4720-4dd0-bfc6-572597257d10.png">
  </p>
<div/>

<div>
    <h1 align="center">Scheme service interaction: </h1><br/>
  <h6>folder client -> class StatClient (with RestTemplate)
    <h6/>
<div/>

<div>
  <p align="center">
    <img width="483" alt="scheme service interaction" src="https://user-images.githubusercontent.com/96682553/191896256-860d1294-da67-4144-86dc-cd03737c9f54.png">
  </p>
<div/>

<div>
  <h1 align="center">database schema (ewm main service): <br/></h1>
<div/>

<div>
  <p align="center">
    <img width="920" alt="ewm main service database schema" src="https://user-images.githubusercontent.com/96682553/191826289-07e19f22-7cd8-4998-b9e4-09dadd4f2323.png">
 </p>
<div/>
