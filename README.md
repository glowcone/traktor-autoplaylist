# Traktor Autoplaylist
Turn your Traktor playlists into smart, auto-updating ones.
This Java program checks for new music and adds them to your playlists automatically based on lots of rules (genre, BPM, key, etc. as well as many special fields like comment2, remixer, catalog...), by editing Traktor's collection.nml. Has regex and match-case support. Also acts as a launcher: it sorts your tracks and then opens Traktor.
  
![Screenshot](http://i.imgur.com/GC9SAdc.png)![Screenshot 2](http://i.imgur.com/FFrZAXH.png)

## How to use:
1. Download [traktor-autoplaylist.zip](https://github.com/recurza/traktor-autoplaylist/raw/master/Traktor%20Autoplaylist.zip) and double click.  
2. On first run, it will ask you where collection.nml is. It's usually somewhere in your Documents folder.  
3. Click on playlists and edit rules. Then click "Start" to sort your tracks.  
4. Traktor will automatically run. (Tip: replace your Traktor shortcut with this one)  

## Notes:
- If your collection.nml breaks, don't worry, it made a backup (collection.nml.backup)
- DECIMAL\_BPM and LOCK are yes/no questions. It assumes that whatever you type in the box means "true". So, to have a playlist of unlocked tracks, use "LOCK isn't true". Same with DECIMAL\_BPM ("is BPM decimal?").
- I've only tested this on my own library (of 448 tracks), which took ~1.5 seconds with 8 rules.

## Known bugs/Things to improve:
- Playlists cannot have the same names
- **Completely untested on Windows**
- Cannot detect if you removed a playlist in traktor
- It scans and sort the entire file every time

---
*I am in no way affiliated with Traktor or Native Instruments.*
