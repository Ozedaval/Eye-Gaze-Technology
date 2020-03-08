# Techlauncher Eye-Gaze-Technology
<h2><a name = "content"> Table of Contents </a></h2>

<a href = "#Title1"><b> 1. Project Description </b></a><br/>
<a href = "#Title2"><b> 2. Project Administration </b></a><br/>
<a href = "#Title3"><b> 3. Traceability of Progress </b></a><br/> 
<a href = "#Title5"><b> 4. Technical Tools and Constraints </b></a><br/>


<br />

<h2><a name = "Title1"> 1. Project Overview </a></h2>


Eye gaze technology has been widely used to capture eye sights through infrared cameras to control panels without hand click events. While there are critical expenses on infrared cameras which make the eye gaze technology no longer affordable to the general families. This project is proposed to seek for solutions to replace infrared cameras with mobile cameras which would be much more affordable and accessible. Considering that our end-users may have neural inabilities that they may have great difficulties to manage their body coordination and sound their voices. Our project proposes a communication assistance application via face tracking and eye gaze technology to enhance them to select words and draft sentences. Hopefully, their feelings and thoughts could be expressed through our designed user interface.

<h3> 2.4 Stakeholders </h3>

#### End-users:
Our project serves children with neural diseases that affects their body coordination, which means they cannot control their hands to click on mobile phones. Besides, our end-users may not have verbal capacity to sound their voices and needs. They may struggle with
                <ul>
                <li>Amyotrophic lateral sclerosis(ALS)</li>
                <li>Parkinson's Disease</li>
                <li>Brain injury</li>
                <li>Cerebral Pulsy</li>
                <li>Autism</li>
                </ul>
                
#### Client:
Our client raises great expectations to minimize the expenses on the hardware used in the eye gaze technology which merely seek solutions to replace infrared cameras. Both eye gaze technology and face tracking are permitted that no involvement of hand or verbal inputs.
<br />

<h2><a name = "Title2"> 2. Project Administration </a></h2>

<h3> 2.1 Deliverables</h3>

### Deliverable 1: Prototype (Semester 1: Before Week 5):
  * This prototype to showcase the conversion of facial inputs as touch & click events. 
  It will be able to track facial inputs  whether the eye is open or not & the angle of the head such that it can be used to select & operate the Application UI.
     - Researching for a Trustworthy API(s) to recognise facial expression.
     - Implement the Logic of the App which can use the API to convert into touch & click events.
### Deliverable 2: Basic Skeleton of App (Semester 1: Before start of Mid Semester Break)
  * The basic skeleton will consist of what the overall app will look like.This will be built upon the prototype built in the previous deliverable.
    - Creating basic content for assisting communication.
    - Temporarily store content in a persistent storage.
    - Creating a local Database to store those content.
    - Implement the application which follows a good software design pattern/model(MVVM or MVP).
    - Unit Testing of the features.
### Deliverable 3:Minimum Viable Product-MVP (Semester 1: Before Week 7)
  * A MVP will be delivered such that it can allow users can communicate to another person by facial inputs.The application will be able to speak out the word selection made by the user.Furthermore,The sensitivity of the control can be altered.
     - Linking up the database to the application.
     - Adding  Input Sensitivity Settings.
     - Uses an API to convert Text into Speech & store it in the local Storage.
     - Add the local Storage path for each word along with it in the Database.
     - Linking Images & making the UI more friendly. 
     - Unit Testing of the features. 
### Deliverable 4: Customisable content feature (Semester 1: Before Week 9)
  * Users are able to add their own words to the App such that they can use that word to communicate with the help of the App.
    - Add the feature to add words to the database. 
    - Implement Observable design pattern to update UI.
    - Add the feature to delete selected words from the database
### Deliverable 5 (Semester 1: Before Week 10)
* **Integrated Testing & stabilisation**
  
 

<h3> 2.2 Milestones</h3>




<h3> 2.3 Team Roles </h3>

Project Manager (Minju)
 - Responsible to ensure that software development is suitably goverened and reported on
 - Primary client liason
 - Responsible for Project Management Platforms (Jira & Confluence)
 
#### Technical Lead (Maojun)
 - Works with the developers to articulate technical goals
 - Works with the Project Manager to prioritise technical goals into an agreed plan that ensures quality and a timely delivery 

#### Developers (Kalai & Soobin)
 - Responsible for scoping cost, effort and timelines to deliver the technical solutions 
 - Builds the deliverables and communicates the status of the software components to the Technical Lead and Project Manager


<h2><a name = "Title3"> 3. Traceability of Progress</a></h2>

https://comp3500.atlassian.net/jira/software/projects/MEGT/boards/1

Guest Account:
 - ID: audit.techlauncher@outlook.com
 - PW: 2020techlauncher

<h2><a name = "Title3"> 4. Technical Tools and Constraints</a></h2>

**Technical Tools:**
 - Development plateform: Android Studio
 - Eye gaze technology library: OpenCV
 - Face tracking library: Mobile Vision?

**Project Constraints:**
 - No hand motions
 - No verbal inputs
 - Assume that the end-users may have the capacity to focus their eye gaze and control head motions
 - Affordability of the technology to ensure the cost on software and hardware are low



