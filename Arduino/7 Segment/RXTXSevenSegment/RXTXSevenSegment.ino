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

void setup() {
  Serial.begin(9600);
  randomSeed(analogRead(0));
  
  // put your setup code here, to run once:
  xpos = ypos = 0;
  intensity = 0;
  xincrement = 1;
  
  for(int i=22; i<=53; i++) {
    pinMode(i,OUTPUT);
  }
}

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
  delayMicroseconds(100);
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

// Main code to be run repeatedly
void loop() {
  // if serial port is available, read incoming bytes
  if (Serial.available() > 0) {
    recv = Serial.read();
 
    // if 'y' (decimal 121) is received, run the program
    // anything other than 121 is received, turn everything off
    if (recv == 121){  
      // Output x and y position
      sevenOutput(3,xLED,xSeg,xpos);
      sevenOutput(3,yLED,ySeg,ypos);
      
      // Output light intensity
      sevenOutput(4,intLED,intSeg,intensity);
    
      // Incrementing, x goes up to 300 then comes back down again
      xpos = xpos+xincrement;
      if(0==xpos || 300==xpos) {
        xincrement = xincrement*-1;
        intensity = random(0,1025);
        if(300==ypos++) {
          ypos = 0;
        }
      }
    } else { // Turn everything off
      for(int i=22; i<51; i++) {
        digitalWrite(i,LOW);  
      }
    }
     
    // confirm values received in serial monitor window
    Serial.print("--Arduino received: ");
    Serial.println(recv);
  }
}
