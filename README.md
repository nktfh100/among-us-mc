![](https://i.imgur.com/fyfad7m.png)



- Bungeecord support (1 arena per server)
- Reduced vision system!
- Visual tasks
- Dead bodies
- Cameras
- No Flicker scoreboard
- Everything is configurable!
- Arena selector GUI
- Arena setup GUI
- Stats! (MySQL / flat files)
- PlaceHolderAPI support
- VentureChat support
- Holograms
- Minimaps
- Join signs
- Tasks available: wiring, scan (visual), download data, upload data, divert power, accept diverted power, unlock manifolds, empty garbage (visual), prime shields (visual), calibrate distributor, start reactor, clear asteroids (visual), refuel, fuel, clean o2, inspect sample, swipe card, chart course, stabilize steering, fill canisters, insert keys, replace water jug, record temperature, repair drill, monitor tree, open waterways, reboot wifi, fix weather node, scan boarding pass, store artifacts
- Sabotages available: lights, comms, reactor meltdown, oxygen



![](https://i.imgur.com/8VcI50i.png)



- **/au join** - Join arena with the most players
- **/au joinrandom**- Join a random arena
- **/au join** <arena> - Join arena
- **/au leave** - Leave game
- **/au arenas**- Open arena selector GUI
- **/au cosmetics**- Open the cosmetics GUI



- **/aua reload** - Reload configs
- **/aua setup <arena>** - Open setup GUI for arena
- **/aua listarenas** - List all created arenas
- **/aua createarena <name> <min players> <max players> <imposters>** - Create arena
- **/aua addlocation <arena> <location name>** - Add location name to arena
- **/aua setsetting <arena> <setting to change>** - Change various arena settings
- **/aua setmainlobby** - Set main lobby location
- **/aua start <arena>** - Start game
- **/aua endgame <arena>** - End game



![](https://i.imgur.com/U8VZNDo.png)




- **amongus.admin -**Permission for everything admin related
- **amongus.admin.setup -**Allows to setup arenas
- **amongus.admin.startgame -**Allows use of the /aua start command
- **amongus.admin.endgame -**Allows use of the /aua endgame command
- **amongus.perk.double-imposter-chance** - Players with this permission will have higher chance to become the imposter
(if enabled in the config)




![](https://i.imgur.com/adamftQ.png)


<details>
  <summary>Configs</summary>
     
[config.yml](https://pastebin.com/n1GLEG3Z)

[messages.yml](https://pastebin.com/WezWTPkY)

[items.yml](https://pastebin.com/RnBdAvyL)

[sounds.yml](https://pastebin.com/X3y71a97)

[cosmetics.yml](https://pastebin.com/5gJ7Qmq5)

</details>



![](https://i.imgur.com/Fc4utSV.png)


<details>
  <summary>Media</summary>
  
**Clear asteroids task:**

![](https://i.imgur.com/SVsCFFw.gif)

**Unlock manifolds task:**

![](https://i.imgur.com/btXyhPc.gif)

**Submit scan visual task:**

![](https://i.imgur.com/yFalxKo.gif)

**Cameras:**

![](https://i.imgur.com/PVa7iFB.gif)

**Clear asteroids visual task:**

![](https://i.imgur.com/kNRFNan.gif)

**Top-down camera example:**

![](https://i.imgur.com/d0FrbJx.gif)

**Minimap:**

![](https://i.imgur.com/4dBQaAS.jpg)

**Join sign:**

![](https://i.imgur.com/LhoDbHt.jpg)

</details>

**Setup tutorial by Barbercraft:**

[![](http://img.youtube.com/vi/U67PcAZE7Qw/0.jpg)](http://www.youtube.com/watch?v=U67PcAZE7Qw "Setup tutorial by Barbercraft")



![](https://i.imgur.com/0fhIzRz.png)


<details>
  <summary>API</summary>

```java
// Get arena by name
Arena arena = AmongUsApi.getArena("arena");
// Get player info object
PlayerInfo pInfo = AmongUsApi.getPlayerInfo(player);

// Get a copy of the player stats
// Should always run this async!
HashMap<String, Integer> playerStats = AmongUsApi.getPlayerStats(player);

// Events:
   @EventHandler
    public void arenaGameStateChange(AUArenaGameStateChange ev) {
        ev.getArena();
        ev.getNewGameState();
    }

    @EventHandler
    public void arenaStart(AUArenaStart ev) {
        ev.getArena();
    }

    @EventHandler
    public void arenaPlayerLeave(AUArenaPlayerLeave ev) {
        ev.getArena();
        ev.getPlayer();
    }

    @EventHandler
    public void arenaPlayerJoin(AUArenaPlayerJoin ev) {
        ev.getArena();
        ev.getPlayer();
        // Cancellable
}

@EventHandler
public void arenaPlayerDeath(AUArenaPlayerDeath ev) {
    ev.getArena();
    ev.getPlayer(); // victim
    ev.getKilled(); // killed or ejected
    ev.getKiller(); // if killed
}
```

</details>


![](https://i.imgur.com/VUrebSs.png)


- **HolographicDisplays**
- **ProtocolLib**
- **[PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/) (For cosmetics)**



![](https://i.imgur.com/NdHPBQY.png)


<details>
  <summary>pre-configured arena</summary>

There is a Polus map and a Skeld map in here.
[LINK](https://www.mediafire.com/file/l0eirwwmtrx7epx/AmongUs_Pre_Configured_arena.rar/file)
</details>


<details>
  <summary>PlaceholderAPI</summary>
```
%amongus_games_played%
%amongus_imposter_wins%
%amongus_crewmate_wins%
%amongus_total_wins%
%amongus_imposter_kills%
%amongus_tasks_completed%
%amongus_emergencies_called%
%amongus_bodies_reported%
%amongus_times_murdered%
%amongus_times_ejected%
%amongus_time_played%
%amongus_time_played_minutes%
%amongus_time_played_hours%
%amongus_time_played_days%
```

</details>

<details>
  <summary>IMPORTANT: if you want to copy arena</summary>
If you want to copy an arena or something you must delete "mapids" in the arena config
otherwise, minimaps in that arena won't work.
</details>

<details>
  <summary>Arena join sign</summary>
First line: [au]
Second line: arena name

![](https://i.imgur.com/Pp3xyJD.jpg)

</details>

<details>
  <summary>Door setup example</summary>
Note: once you set the two corners of the door it will be replaced with fake blocks only shown to you. (right-click to remove them)

![](https://i.imgur.com/BIaU6U3.gif)

</details>

<details>
  <summary>Asteroids visual task setup example</summary>
The cannons will fire in the direction you are facing.
 
![](https://i.imgur.com/s4uvUd4.gif)

</details>

**[Support Discord](https://discord.gg/KGRbaqts33)**

