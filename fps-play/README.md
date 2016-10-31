Our aim is to replicate an FPS player with software, so we can play
the game through a virtual player. Virtual player software will
process raw images coming from the game and send key messages to move
around, and shoot. This project's code wil not be a "bot" because
usual bots can and do make game API calls and get direct
information. Our job is harder, our software will need to process
images, run pattern recognition, calculate features, etc. in order to
"see" inside the game, just like a regular "human" player would. 

#Installation

=============================================================
MAIN IDEA
=============================================================

We make sure UrbanTerror is started at a speficific location on the
screen, in a window (not maximized). fps-play takes a snapshot of the
desktop continuously and feeds this series of images to a recognizer /
player "program". Once an action is decided the keystrokes are sent to
the game just like a regular user. 

xdotool

In order to send keystrokes to UrT, you need to install xdotool

http://www.semicomplete.com/projects/xdotool/

UrbanTerror 

Download install UrbanTerror, say under [DIR]/UrbanTerror. 

In order to play against bots on your local machine, you need to start
your own server. Under [DIR]/UrbanTerror/q3ut4/server.cfg add this

set bot_enable "1"

On [DIR]/UrbanTerror/q3ut4/bots.cfg file

addbot boa 4 blue 25 "Bot 1"
addbot chicken 4 blue 25 "Bot 2"
addbot cockroach 4 red 25 "Bot 3"
addbot goose 5 red 25 "Bot 4"

For testing purposes, you need to run UT in windowed mode, and we turn
off all unnecessary messages, displays, etc, add this to your
q3ut4/autoexec.cfg file

seta r_fullscreen "0"
seta cg_maptoggle "0"
seta cg_showbullethits "0"
seta g_drawteamoverlayscores "0"
seta cg_drawteamscores "0"
seta cg_drawStatus "0"
seta cg_drawRewards "0"
seta cg_drawFPS "0"
seta cg_drawTimer "0"
seta cg_drawCrosshairNames "0"
seta cg_drawAmmoWarning "0"
seta cg_standardChat "0"
seta cg_teamchatsonly "0"
seta cg_novoicechats "0"
seta g_loghits "0"
seta g_logroll "0"
seta cg_drawIcons "0"
seta cg_drawteamoverlay "0"
seta cg_hudweaponinfo    0
seta cg_lagometer "0"
seta cg_speedo "0"
seta cg_crosshairHealth "0"
seta cg_autoradio "0"
seta cg_noTaunt "0"
seta cg_noVoiceText  "0"
seta cg_drawcrosshair "0"
seta cg_drawhands 0

=============================================================
TESTING
=============================================================

Use server.sh and client.sh scripts in our code.

Then, execute online.py. 
