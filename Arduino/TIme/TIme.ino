#include <Servo.h>
#include <Stepper.h>

const int stepsPerRevolution = 400;
Stepper myStepper(stepsPerRevolution, 2, 3, 4, 5);

class Flasher
{
    // Class Member Variables
    // These are initialized at startup
    int ledPin;      // the number of the LED pin

    int updateintervalsen = 1;
    // These maintain the current state
    int ledState;                 // ledState used to set the LED
    unsigned long previousMillissen;   // will store last time LED was updated
    int count = 0;
    int count2 = 0;
    int intenval[1000];

    // Constructor - creates a Flasher
    // and initializes the member variables and state
  public:
    Flasher(int pin)
    {
      ledPin = pin;
      pinMode(ledPin, INPUT);

      previousMillissen = 0;
    }

    void Updatesen()
    {
      // check to see if it's time to change the state of the LED
      unsigned long currentMillis = millis();

      if ((currentMillis - previousMillissen >= updateintervalsen))
      {
        previousMillissen = currentMillis;  // Remember the time
        count++;
        Serial.print(millis());
        Serial.print("    ");
        Serial.print(count2);
        Serial.print("    ");
        Serial.print(count);
        Serial.print("    ");
        Serial.println(analogRead(0));
        if (count == 30000)
        {
          count = 0;
          count2++;
        }
        if (count2 == 3)
        {
          Serial.print("Picture Done!");
          delay(100000);
        }
      }

    }
};


class Sweeper
{
    Servo servo;              // the servo
    int pos;              // current servo position
    int increment;        // increment to move for each interval
    int  updateInterval;      // interval between updates
    unsigned long lastUpdate; // last update of position

  public:
    Sweeper(int interval)
    {
      updateInterval = interval;
      increment = 1;
    }

    void Attach(int pin)
    {
      servo.attach(pin);
    }

    void Detach()
    {
      servo.detach();
    }

    void Update()
    {
      if ((millis() - lastUpdate) > updateInterval) // time to update
      {
        lastUpdate = millis();
        pos += increment;
        servo.write(pos);
        //Serial.println(pos);
        if ((pos >= 30) || (pos <= 0)) // end of sweep
        {
          // reverse direction
          increment = -increment;
        }
      }
    }
};

class Steppermotor
{
    int updateIntervalstep;
    unsigned long lastUpdate;

  public:
    Steppermotor(int intervalstep)
    {
      updateIntervalstep = intervalstep;
    }

    void Update()
    {
      if ((millis() - lastUpdate) > updateIntervalstep) // time to update
      {
        lastUpdate = millis();
        myStepper.step(-1);
        delay(1);
        myStepper.step(-1);
      }
    }
};

Sweeper sweeper1(15);
Flasher sen1(0);
Steppermotor step1(1000);
int m = 0;

void setup()
{
  Serial.begin(115200);
  myStepper.setSpeed(60);
  sweeper1.Attach(9);
}


void loop()
{
  sweeper1.Update();

  sen1.Updatesen();

  step1.Update();

  /*Serial.println(millis());
    Serial.println(m);
    m++;
    Serial.println(analogRead(0)); */


}
