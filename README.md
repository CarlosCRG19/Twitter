# Project 3 - Twitter

**Twitter** is an android app that allows a user to view their Twitter timeline and post a new tweet. The app utilizes [Twitter REST API](https://dev.twitter.com/rest/public).

Time spent: **20 (6 required / 14 on stretch stories)** hours spent in total

## User Stories

The following **required** functionality is completed:

* [x]	User can **sign in to Twitter** using OAuth login
* [x]	User can **view tweets from their home timeline**
  * [x] User is displayed the username, name, and body for each tweet
  * [x] User is displayed the [relative timestamp](https://gist.github.com/nesquena/f786232f5ef72f6e10a7) for each tweet "8m", "7h"
* [x] User can **compose and post a new tweet**
  * [x] User can click a “Compose” icon in the Action Bar on the top right
  * [x] User can then enter a new tweet and post this to Twitter
  * [x] User is taken back to home timeline with **new tweet visible** in timeline 
  * [x] Newly created tweet should be manually inserted into the timeline and not rely on a full refresh
* [x] User can **see a counter with total number of characters left for tweet** on compose tweet page
* [x] User can **pull down to refresh tweets timeline**
* [x] User can **see embedded image media within a tweet** on list or detail view.

The following **optional** features are implemented:

* [x] User is using **"Twitter branded" colors and styles**
* [x] User sees an **indeterminate progress indicator** when any background or network task is happening
* [x] User can **select "reply" from home timeline to respond to a tweet**
  * [x] User that wrote the original tweet is **automatically "@" replied in compose**
* [x] User can tap a tweet to **open a detailed tweet view**
  * [x] User can **take favorite (and unfavorite) or retweet** actions on a tweet
* [x] User can view more tweets as they scroll with infinite pagination
* [x] Compose tweet functionality is built using modal overlay
* [x] User can **click a link within a tweet body** on tweet details view. The click will launch the web browser with relevant page opened.
* [x] Replace all icon drawables and other static image assets with [vector drawables](http://guides.codepath.org/android/Drawables#vector-drawables) where appropriate.
* [ ] User can view following / followers list through any profile they view.
* [x] Use the View Binding library to reduce view boilerplate.
* [ ] On the Twitter timeline, apply scrolling effects such as [hiding/showing the toolbar](http://guides.codepath.org/android/Using-the-App-ToolBar#reacting-to-scroll) by implementing [CoordinatorLayout](http://guides.codepath.org/android/Handling-Scrolls-with-CoordinatorLayout#responding-to-scroll-events).
* [x] User can **open the twitter app offline and see last loaded tweets**. Persisted in SQLite tweets are refreshed on every application launch. While "live data" is displayed when app can get it from Twitter API, it is also saved for use in offline mode.

The following **additional** features are implemented:

* [x] Lots of UI improvement (UI inspired by Omar Ali Badr: https://www.behance.net/gallery/102573777/Twitter-App-UIUX-Redesign)
* [x] Count of favorites and retweets are displayed
* [x] Interactions can be done on the main timeline (not only in detail view) and changes persist between activities  

## Video Walkthrough

Here's a walkthrough of implemented user stories:

<img src='walkthrough.gif' title='Video Walkthrough' width='400px' alt='Video Walkthrough' />

### Gif with Part 2 of stories (Persistance with SQLite and Reply)

<img src='walkthrough_extra.gif' title='Video Walkthrough' width='400px' alt='Video Walkthrough' />

GIF created with [Kap](https://getkap.co/).

## Notes (Challenges encountered while building the app.)

* Refactor the ComposeActivity into a Fragment. At the beginning it was difficult to understand that the fragment exists on an activity and therefore it is a good idea to use an interface that will be implemented by said activity. This also allows us to transmit information between the fragment and the activity as if we were using a startActivityForResult.
* Logic for the transmission of tweet modifications between activities (mainly, between TweetDetailsActivity and TimelineActivity). In the end, I had to start the detail view expecting a result and use an intent that would pass the tweet and its position in the RV as extras. A similar intent is used to pass modifications from TweetDetailsActivity to TimelineActivity and in the latter the adapter is notified that there were changes to a specific position.
* During the demo, a bug appeared in which, when refreshing some tweets at the beginning, they change their like or retweet icon even without being pressed (no request is made and the changes do not persist when passing to the detail view). I could not identify the cause of this before submission time, but my first idea is that the adapter could be passing modifications of the tweets to the wrong positions.

## Additional features ideas for the future

* Include dark mode. Refer to this guide https://www.geeksforgeeks.org/how-to-implement-dark-night-mode-in-android-app/ (Provided by Ainsley, my manager)
* Add current user information at the top left of the app (profile pic, name and screen name). This can be done in the login activity doing a request to get the login credentials (GET account/verify_credentials).
* Click on image to open it
* Finish missing stretch stories. The list of followers can be obtained with another request (GET followers/list) so a new UserDetailActivity can be created in which both lists can be displayed (maybe, with a TabLayout)


## Open-source libraries used

- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Android

## License

    Copyright 2021 Carlos Rodriguez

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
