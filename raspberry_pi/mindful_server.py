import socket
import logging
import traceback
import sys

import RPi.GPIO as GPIO

PORT = 12345
LED_PIN = 23

if __name__ == "__main__":
    # Set logging format
    FORMAT = '[%(asctime)s %(filename)15s line: %(lineno)5s] %(message)s'
    logging.basicConfig(stream=sys.stdout, format=FORMAT, level=logging.DEBUG)
    log = logging.getLogger("mindful_server")

    # Set the GPIO
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(LED_PIN, GPIO.OUT)

    # Get a server socket
    s_sock = None
    s_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s_sock.bind(("", 12345))
    s_sock.listen(5)

    current_state = 0
    while True:
        try:
            # Accept incoming socket connections
            log.info("Accepting incoming socket connections")
            (c_sock, add) = s_sock.accept()
            log.info("Accepted socket connection from address: {}".format(add))
            data = c_sock.recv(5)
            log.info("Received: {}".format(data))
            current_state = ~current_state
            log.info("Current State: {}".format(current_state))
            GPIO.output(LED_PIN, GPIO.LOW if not current_state else GPIO.HIGH)
            c_sock.close()

        except Exception as e:
            log.error(traceback.format_exc())

    GPIO.cleanup()
