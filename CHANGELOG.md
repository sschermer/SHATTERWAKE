# Changes

## 0.3.0-alpha — 2026-11-15
- Corrected excessive ease of v0.2 with moderate enemy and projectile pressure; 9-second opening grace, faster spawn and gradual projectile caps.
- Lower star rewards and much slower passive star supply; rebalanced progressive shop prices to prevent all-max upgrades by end of run.
- Boss entrance sequence: stop new spawns; wait for enemy field to clear; purge old hostile shots; blink centered boss warning with a 3-second synthesized siren; animate boss from above over 3.7 seconds.
- Boss collision and attacks disabled during fly-in; short protection grace at start of combat.
- Additional original pseudo-isometric stage art: parallax clouds, wreckage, machine frameworks, glowing crystals, storm towers and citadel structures.
- Additional deterministic regression tests for boss transition and star economy.
- No external music, textures or third-party game libraries added.


## 0.2.0-alpha — 2026-11-15
- Player-reported balance regression: first run ended in approximately 30 seconds.
- Opening 18 seconds: enemy projectiles disabled; next 37 seconds: at most three simultaneously active hostile shots.
- Progressive wave sizes and caps; fewer, slower enemies; fewer enemy bullets; longer fire intervals.
- Increased starting hull from five to six and post-hit protection from 1.5 to 2.4 seconds.
- Reduced boss volley sizes and frequencies; made opening boss less durable.
- Added multi-layered gradient-shaded craft, enemy and boss components, improved engine glows, debris and projectile effects.
- 26 pure-Java game-rule checks including early-game pacing.
- Continued procedural original artwork, no external asset packages.


## 0.1.0-alpha — 2026-11-15
- Native Java/Android Canvas arcade playable prototype.
- Procedural pseudo-isometric art and generated audio, no third-party runtime libraries.
- Five distinct worlds, three stages per run, two route choices per run.
- Multiple enemies, 12-piece segmented worm and four alternative multi-stage bosses.
- Automatic aim correction, two control schemes, missile/EMP and four on-demand abilities.
- Star economy, post-boss shop, technology archive, score and save, two paid continues.
- Legal inventory, licensing and privacy documents; GitHub Actions debug APK build.
