# Project Game Stew

## Team Members
- @Maxim.Medlyarskiy
- @Jakob.Harych 
- @Christoph.Friedrich

## Problem Overview
Board game nights often default to familiar favorites like Monopoly and Catan, leaving new and potentially exciting games unplayed due to perceptions of complexity and resistance to trying something new.

## Solution
Game Stew is a web-app designed to introduce board game enthusiasts to new games by matching them with titles similar to those they already enjoy. This interactive tool simplifies the discovery process by creating a virtual "stew" of user-selected games to recommend a new, similar game.

## Key Features
- **Interactive Search:** Users can select familiar games to generate recommendations for new, similar games.
- **Customization:** The app allows users to maintain a digital record of their game collection and rate games based on specific attributes, enhancing future search accuracy.
- **User-Friendly Design:** A minimalistic and engaging interface helps mitigate decision fatigue and encourages exploration of new games.

## Objective
To broaden gaming horizons and enhance board game nights by making the discovery of new games as engaging and simple as playing a classic.



## Links
- [Class Diagram Miro Board](https://miro.com/app/board/uXjVKYY2OXE=/)
- [Style Guide](style_guide.md)

## Todo
### Implement Basic Front-End
The following is a loose collection of notes:
1. Stew
   - Server hat Stew an Session gebunden
   - gibt HTML zurück (mit Stew)
   - User fügt hinzu/entfernt
2. Recommendation
   - HTML Template
   - User fügt zu Collection hinzu
   - User fügt Property zu stew hinzu

3. Collections
   - HTML Template

4. Collection View
   - HTML Template

### Implement Recommendation Algorithm
Also just some loose notes:
``Stew.taste``
1. get (some) games from DB (filtered)
   - filter by stew filters
   - only games with at least one property in common?
       * how do you do this with SQL again?
   - only games with score >= x?
   - exclude based on allergies?
   - make sure not to retrieve too many games
2. rank games by similarity
   - for each game in stew:
       * for each game property:
         - if game property exactly in target game: +1 score
         - if property kind of in target game: +~0.25 score
   - modify score for properties in stew
3. return top game