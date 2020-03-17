# Techlauncher Eye-Gaze-Technology
<h2><a name = "content"> Table of Contents </a></h2>

<a href = "#Title1"><b> 1. Project Description </b></a><br/>
<a href = "#Title2"><b> 2. Project Administration </b></a><br/>
<a href = "#Title3"><b> 3. Traceability of Progress </b></a><br/> 
<a href = "#Title4"><b> 4. Technical Tools and Constraints </b></a><br/>


<br />

<h2><a name = "Title1"> 1. Project Overview </a></h2>

According to the World Health Organization, roughly 15% of the world's population live with some form of disability. There is a technology that helps people with disabilities to communicate better in lives. The major barrier however in accessing this technology is it's high cost that most people cannot afford. To promote the inclusivity of this technology, we are seeking to recreate this technology to leverage the smart phone platform.

The Eye Gaze technology has been widely used to capture eye movement and focus through infrared cameras and in turn control the screen display without physical interaction. Infrared cameras, developed at the quality required for eye tracking, are expensive. This makes the final product of eye gaze technology no longer affordable to the general populace. This project seeks for solutions to replace infrared cameras with smart phone cameras which are much more affordable and accessible. Our project proposes a communication assistance application via face and eye tracking to enable them to select words and compose sentences for disabled people. We seek to improve their ability to communicate their feelings and thoughts more effectively through our designed user interface.

<h3> Stakeholders </h3>

#### End-users:
Our project serves children with neural diseases that affects their body coordination, which means they cannot control their hands to click on mobile phones. Besides, our end-users may not have verbal capacity to sound their voices and needs. They may have:-
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

<h3> 2.1 Schedule</h3>

#### Define Requirements & Research (Semester 1: Before Week 6):
  * Design thinking should be adopted to analyze difficulties our end-users may confront daily when they may fail to voice their needs. The project specifications should be drafted to define these problems to ensure the values delivered to children with neural disease.
    - The project should ensure that our end-users could learn how to use the functions of software with low barriers.
    - The project should cover that major communications needs of our end-users in their daily life, such as their emotional expressions, and Food demands
    - The project should cover the requirements from our end-users' parents that their concerns and expectations on communication features would be prioritized.
    
    
#### Deliverable 1: Prototype (Semester 1: Before Break Week 1):
  * Analysis on the technology supportive libraries on mobile devices.This prototype to showcase the conversion of facial inputs/eye gaze as touch & click events. Either succuessful adaption will be further modified as final solution.
      > Face Tracking: It will be able to track facial inputs whether the eye is open or not & the angle of the head such that it can be used to select & operate the Application UI.
      
      > Eye Gaze: It will be able to track the eye gaze on the screen and concentrate the attention into a spot which will change smoothly as eye gaze shifts. 
     - Researching for a Trustworthy API(s) to recognise facial expression/eye gaze.
     - Implement the Logic of the App which can use the API to convert into touch & click events.
     
#### Deliverable 2: Basic Skeleton of App (Semester 1: Before Week 7)
  * The basic skeleton will consist of what the overall app will look like.This will be built based upon the Logic part of the prototype built in the previous deliverable.
    - Creating basic content for assisting communication.
    - Temporarily store content in a persistent storage.
    - Implement the application which follows a good software design pattern/model (MVVM or MVP).
    - Unit Testing for the added features if applicable.
#### Deliverable 3:Minimum Viable Product-MVP (Semester 1: Before Week 10)
  * A MVP will be delivered which could successful adapt the eye gaze or face tracking technology into our android application that users may manage to touch and click screens to help them sound their voices with basic sentence structures.
     - Using a local database to store words & related data.
     - Use an API to convert Text into Speech & store it in the local Storage.
     - Adding Input Sensitivity Settings
     - Linking Images & making the UI more friendly. 
     - Unit Testing of the added features if applicable. 

#### Integrated Testing (Semester 1: Week 10 - Week 12)
  * Integrated testing will be implemented to make sure the app is stable & does not have any major bugs.
    - Implement Integrated Testing.
    - Check for any major bugs & fix them if found.   

   

<h3> 2.2 Milestones</h3>

#### Overview of Milestones
<img src="Resources/Schedule.png" width="40%" length="40%">


 #### Detailed Milestones
 <a href = "#JiraAccess"><b> Use Jira to Access </b></a><br/>



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

<a name = "JiraAccess"/>
<h2><a name = "Title3"> 3. Traceability of Progress</a></h2>


<a href = "https://comp3500.atlassian.net/jira/software/projects/MEGT/boards/1/roadmap"><b> Jira Project Management Platform </b></a><br/>



[GuestAccount](https://id.atlassian.com/login?application=jira&continue=https%3A%2F%2Fcomp3500.atlassian.net%2Flogin%3FredirectCount%3D1%26dest-url%3D%252Fjira%252Fsoftware%252Fprojects%252FMEGT%252Fboards%252F1%252Froadmap%26application%3Djira&email=audit.techlauncher%40outlook.com) (Note: Copy _PW_ before clicking GuestAccount)
 - _ID_: audit.techlauncher@outlook.com
 - _PW_: 2020techlauncher

<h2><a name = "Title4"> 4. Technical Tools and Constraints</a></h2>

**Technical Tools:**
 - Development platform: Android Studio
 - Face & eye tracking library: Google Mobile Vision API
 - Eye gaze technology library: OpenCV
 

**Project Constraints:**
- Affordability of the technology to ensure the cost on software and hardware are low.
 - No hand motions.
 - No verbal inputs.
 - Assume that the end-users may have the capacity to focus their eye gaze and control head motions.
 



