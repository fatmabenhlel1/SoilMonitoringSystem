#!/usr/bin/env python3
import RPi.GPIO as GPIO

MOISTURE_PIN = 17  # Changez selon votre GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setup(MOISTURE_PIN, GPIO.IN)

value = GPIO.input(MOISTURE_PIN)
print(value)

GPIO.cleanup()
