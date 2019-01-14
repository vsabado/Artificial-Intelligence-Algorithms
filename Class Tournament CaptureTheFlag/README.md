# aitournament

# Instruction

http://csce.uark.edu/~mgashler/ai/a8/instructions.html

## Best AI Wins

* Deadline - Dec 3
* 1 Java file

## Ideas
* Genetic algorithm 
* Analyze the map (random in the tournament)
* Decision based on AI Healths and Flag health
* Adaptive AI depending on the opponent?
* Add ability save current algorithm
* Add ability to easily the battle
* Add ability to hardcode things
* 2 Algorithms, Uniform search, A* algorithms, Minimax
* Fork the game and simulate
* 1 second Time Balance
* Make move based on current time balance
* Machine learning?
* Take some ideas from Startcraft AI
* Is it possible to check game states? See other AIs health? Location?
* If so, we can target weak, dying players
* Gang up on 1 opponent at a time
* Gang on on opponent closest to flag
* Add in ability to “Kite” opponent
* Add ability to detect being hit and prioritize survival
* If no opponent alive, stalk or “camp” opponent
* Strategy: Dodge and ignore enemy and just go for the flag
* Attack and Dodge tactic
* Strat: A* search to find the fastest path to the flag and have all 3 rush the flag
* Breakdowns:
** 3 Attackers
**1 Def 2 Attack
**2 Def 1 Attack
**3 Def and move together
* Battle tactic, no opponent tactic, all opponent dead tactic,  
* Make agents wait by the flag and only move away from it once all enemy agents are killed. Creates greater distance between enemy respawn point and enemies. 
* Question: Does units heal? They do
* Detection if being: Loses health

## Agents we have:

### All of the other files implement example agents.

* Human.java implements an agent that takes orders from the mouse. This lets you, the human, play the game. This is useful for enabling you to test the behavior of your other agents by manually battling against them. (This agent assumes it is the blue team--if not, the controls are wierd.) (NO HUMANS)
* SittingDuck.java implements a simple reflex agent. This is the simplest example agent. It waits for a certain period of time, then directs every sprite to attack the opponent's flag.
* AggressivePack.java implements a simple reflex agent. The sprites all band together. They first try to kill off all the opponents, then they attack the flag. Also, they dodge any bombs thrown at them.
* Blitz.java implements a simple reflex agent. It charges the opponent's flag, but also throws bombs at any opponents it passes on the way, and attempts to dodge any bombs that are thrown at it.
* Mixed.java implements a slightly more complex reflex agent. It assigns one sprite to defend the flag, one to attack opponents, and one to charge the flag. All of these agents dodge bombs that are thrown at them.
* PrescientMoron.java is an example agent that utilizes artificial intelligence. It periodically forks the world to simulate some candidate actions so it can anticipate the consequences of those actions. However, it currently only uses this ability to sloppily find its way to the opponent's flag.


## To Do:
* Run the tournament with the current agents and find best winner
* Find current best agent we have atm
* Create custom agents and find their place in the tournament
* See who performs the best
* Best W/L ratio
* Where is current code base? (has to clean, contain provided code and old, but doesn’t combine them)
* Create a private repo for it.
* What does out agent have access to? Game states? Opponent locations? Opponent attacking?
