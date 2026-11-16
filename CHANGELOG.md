## 0.5.0-alpha — understandable training and high-visibility plasma

- Boss final component now triggers a full-screen scale multi-stage detonation with shockwaves, secondary blasts, module wreckage and tangible collectible bonus stars, ability drops and technology chip. Do not enter the Werft while any loot remains in reach; chip technology must be physically collected to unlock.
- After salvage, a mothership approaches and the player ship flies into its illuminated hangar. Die Sternenwerft shows a large modular fighter, a 2-column offer overview and an animated subsystem-specific zoom. Require a separate, post-animation explicit purchase; Back exits preview without spending.
- Replace abruptly finishing five-step onboarding with nine guided tasks and a deliberate WEITER checkpoint after every success.
- Give each of the four power-ups 15 seconds of real, counted, playable training before offering the next lesson; include a timer/progress bar, durable firing targets and harmless shield-test shots. Keep real-run durations unchanged.
- Add hands-on rapid-fire, triple-shot and plasma lessons. Plasma must destroy a visible stationary training enemy; missile lessons likewise end only after a practice kill.
- Trigger missiles and EMP on a single tap in both control modes, removing the ambiguous arm-then-release interaction.
- Give active plasma a pulsing violet ship aura/reactor, high-contrast wide projectiles and visible purple impact particles. A status label accompanies the active HUD icon.
- Retain replay/skip behavior, persisted discovery tips, existing enemy balancing and safe tutorial simulation.

# Changes

## 0.4.0-alpha — 2026-11-16
- Added a guided first-play mission (steering, targeting, star pickup, shield activation and secondary weapon use).
- Persist tutorial completion and first-explanation state; replay tutorial from the main menu.
- Pause simulation to explain each newly collected ability, requiring correct action at the original HUD control. Skip is accessible when needed.
- Added first-shop orientation, ability and secondary glyphs, and active timer bars.
- Replaced original simplified hull geometry with own transparent high-resolution cached sprites for fighter, six enemy classes and five boss module types. Added surface paneling, cockpit glazing, engine nozzles and lighting.
- Gameplay difficulty and existing boss entrance untouched.
- Updated test/build pipeline with pure-Java tutorial state-machine regression tests; no third-party assets or runtime libraries.


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

### Combat motion and training follow-up

- Five-second active power-up trials, visible hostile craft firing in shield tutorial.
- Remove free auto-aim; five upgrade tiers at 3% each, up to 15% correction.
- Curved enemy attack paths, banked turning and rare golden looping star-rich fighter.
