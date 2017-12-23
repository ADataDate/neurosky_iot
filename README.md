# neurosky_iot
This project is an attempt to develop an Android application that will communicate with the Neurosky Mindwave Mobile+ and a Raspberry Pi via sockets to control an IoT Power Relay with "thought".

The goal is to be able to turn off my TV, Fan, or Light, with my mind alone, without having to get up out of bed once I'm in it.

The [Neurosky Mindwave Mobile+](https://www.sparkfun.com/products/14455) headset is a brainwave reading EEG that communicates to devices via Bluetooth. The app will use the Neurosky android development tools to detect attention/focus and blinking and send "on" or "off" commands based on a sliding bar that measures concentration via socket to a [Raspberry Pi](https://www.sparkfun.com/products/13825) that will control an [IoT Power Relay](https://www.sparkfun.com/products/14236).

From the App go to settings and enter the IP address of your Raspberry Pi. Make sure you have wifi enabled on both the phone and the Raspberry Pi and on the same network.


Note: I am a hardware engineer and this is my first attempt at app dev. Be Gentle.


Contributors: Mary West & Ryan Mortenson
Major thanks to Ryan for his patience and expertise. 
