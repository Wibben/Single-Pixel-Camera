#include <Servo.h>
#include <Stepper.h>

const int stepsPerRevolution = 400;
Stepper myStepper(stepsPerRevolution, 2, 3, 4, 5);

const int a=0,b=2,c=4,d=6,e=1,f=3,g=5;

// X position variables
const int xLED[] = {22,23,24}; // LED Power
const int xSeg[] = {25,26,27,28,29,30,31}; // Each individual segment
int xpos,xincrement;

// Y position variables
const int yLED[] = {32,33,34}; // LED Power
const int ySeg[] = {35,36,37,38,39,40,41}; // Each individual segment
int ypos;

// Intensity variables
const int intLED[] = {42,43,44,45}; // LED Power
const int intSeg[] = {47,48,49,50,51,52,53}; // Each individual segment
double intensity;

int recv = 0; // Communication with java


// 7 Segment output based on pins and value to each individual 7 segment
void sevenSeg(int LEDpin, int seg[], int val)
{
  digitalWrite(LEDpin,HIGH);
  // Turn on pins based on value of output
  if(val==0) { // b c d e f g
    digitalWrite(seg[b],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[e],HIGH);
    digitalWrite(seg[f],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==1) { // d g
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==2) { // a d e f
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[e],HIGH);
    digitalWrite(seg[f],HIGH);
  } else if(val==3) { // a c d f g
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[f],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==4) { // a b d g
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[b],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==5) { // a b c f g
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[b],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[f],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==6) { // a b c e f g
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[b],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[e],HIGH);
    digitalWrite(seg[f],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==7) { // c d g
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==8) { // a b c d e f g
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[b],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[e],HIGH);
    digitalWrite(seg[f],HIGH);
    digitalWrite(seg[g],HIGH);
  } else if(val==9) { // a b c d f g
    digitalWrite(seg[a],HIGH);
    digitalWrite(seg[b],HIGH);
    digitalWrite(seg[c],HIGH);
    digitalWrite(seg[d],HIGH);
    digitalWrite(seg[f],HIGH);
    digitalWrite(seg[g],HIGH);
  }
  //delay(1);
  delayMicroseconds(50);
  // Turn everything off for next pin
  digitalWrite(LEDpin,LOW);
  digitalWrite(seg[a],LOW);
  digitalWrite(seg[b],LOW);
  digitalWrite(seg[c],LOW);
  digitalWrite(seg[d],LOW);
  digitalWrite(seg[e],LOW);
  digitalWrite(seg[f],LOW);
  digitalWrite(seg[g],LOW);
}

// Output to 7 segment
void sevenOutput(int s, int LEDpins[], int segpins[], int xpos) 
{
  for(int i=s-1; i>=0; i--) {
    sevenSeg(LEDpins[i],segpins,xpos%10);
    xpos = xpos/10; // Get rid of the last digit
  }
}

class Flasher
{
    // Class Member Variables
    // These are initialized at startup
    int ledPin;      // the number of the LED pin

    int updateintervalsen = 3;//time inbetween readings(should be 3)
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
        /*
        Serial.print(millis());
        Serial.print("    ");
        Serial.print(count2);
        Serial.print("    ");
        Serial.print(count);
        Serial.print("    ");*/
        Serial.println(analogRead(0));
        if (count == 30000)
        {
          count = 0;
          count2++;
        }
        if (count2 == 3)
        {
          //Serial.print("Picture Done!");
          //delay(100000);
        }

        // 7 Segment controls, update intensity
        intensity = analogRead(0);
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

        // 7 Segment controls, update x position
        // Incrementing, x goes up to 300 then comes back down again
        xpos = xpos+xincrement;
        if(0==xpos || 299==xpos) xincrement = xincrement*-1;
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
        //delay(1);
        //myStepper.step(-1);

        // 7 Segment controls, update y position and end program when y = 300
        if(300==++ypos) recv= 0;
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

  xpos = ypos = 0;
  intensity = 0;
  xincrement = 1;

  for(int i=22; i<=53; i++) {
    pinMode(i,OUTPUT);
  }
}


void loop()
{
  // if serial port is available, read incoming bytes
  if (Serial.available() > 0) {
    int temp = Serial.read();
    if(temp == 121) recv = 121;
    else if(temp == 110) recv = 0;
  }
 
  // if 'y' (decimal 121) is received, run the program
  // anything other than 121 is received, turn everything off
  if (recv == 121){  
    sweeper1.Update();
  
    sen1.Updatesen();
    
    step1.Update();
      
    // Output x and y position
    sevenOutput(3,xLED,xSeg,xpos);
    sevenOutput(3,yLED,ySeg,ypos);
      
    // Output light intensity
    sevenOutput(4,intLED,intSeg,intensity);
  } else { // Turn everything off
    for(int i=22; i<53; i++) {
      digitalWrite(i,LOW);  
    }
  }
  
  /*Serial.println(millis());
    Serial.println(m);
    m++;
    Serial.println(analogRead(0)); */


}
