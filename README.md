\## TLDR; As simple as it sounds, it cuts big mp3 file to a shorter files.

\## Longer version A `CLI` tool that reads from input where to cut the mp3, and using `ffmpeg`, it generates multiple mp3 files from a big one.

Great for cutting down sample effect sounds (bought or free of course, no pirating!🤠) for your developed game.

-   Uses `ffmpeg` library to do the cutting, so you&rsquo;ll need to install it on your machine
-   Reads from file where cut
    -   Uses format `[time to cut] [artist] - [track name]`
    -   Sample 1
        
        ```
        0:00:00 Garden ambience - Research1
        0:00:01 Garden ambience - Research2
        0:00:02 Garden ambience - Research3
        0:00:04 Garden ambience - Research4
        0:00:10 Garden ambience - Research5
        ```
