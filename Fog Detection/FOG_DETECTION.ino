#include "DHT.h"
#include <math.h>

#define DHTPIN 21      
#define DHTTYPE DHT22  
#define LDR_PIN 34     
#define LED_PIN 2  

DHT dht(DHTPIN, DHTTYPE);

// --- Missing Variables for Blinking (Inke bina error aa raha tha) ---
unsigned long lastBlinkTime = 0;
const int blinkInterval = 500; 
bool ledState = LOW;

double calculateDewPoint(double t, double h) {
  double a = 17.27;
  double b = 237.7;
  double gamma = ((a * t) / (b + t)) + log(h / 100.0);
  double dp = (b * gamma) / (a - gamma);
  return dp;
}

void setup() {
  Serial.begin(115200);
  pinMode(DHTPIN, INPUT_PULLUP); 
  pinMode(LDR_PIN, INPUT); 
  pinMode(LED_PIN, OUTPUT);
  dht.begin();
  Serial.println("\n--- FOG MONITORING SYSTEM: SCIENTIFIC MODE ---");
}

void loop() {
  float t = dht.readTemperature();
  int ldrRaw = analogRead(LDR_PIN);

  float h = dht.readHumidity();
  double dp = calculateDewPoint(t, h);
  double spread = t - dp; 
  float lightPercent = (ldrRaw / 4095.0) * 100.0;
   

  // --- Artificial var to Activate Fog ---
  // double h = 95.0;
  // double spread = 2.0;
  // float lightPercent = 90.0;

  if (isnan(t)) {
    Serial.println("Sensor Error: Check DHT22 Connections!");
    delay(2000);
    return;
  }

  // Yahan se loop chal raha hai (Pehle yahan galti se } lag gaya tha)
  if (h >= 95.0 && spread <= 2.0 && lightPercent <= 90.0) {
    float fogPercent = (h + (100.0 - (spread * 25.0)) + (100.0 - lightPercent)) / 3.0;
    
    if (fogPercent > 100) fogPercent = 100;
    if (fogPercent < 0) fogPercent = 0;

    Serial.println("\n*******************************");
    Serial.print("STATUS: FOG DETECTED: "); Serial.print(fogPercent); Serial.println("% <<<");
    Serial.println("*******************************");

    // --- BLINKING LOGIC ---
    unsigned long currentMillis = millis();
    if (currentMillis - lastBlinkTime >= blinkInterval) {
      lastBlinkTime = currentMillis;
      ledState = !ledState; 
      digitalWrite(LED_PIN, ledState);
    }
    
  } else {
    Serial.println("\nSTATUS: NO FOG");
    digitalWrite(LED_PIN, LOW);
  }

  Serial.print("Light: "); Serial.print(lightPercent); Serial.print("% (Raw: "); Serial.print(ldrRaw); Serial.println(")");
  Serial.print("Humidity: "); Serial.print(h); Serial.println("% ");
  Serial.print("Temp: "); Serial.print(t); Serial.println("C | ");
  Serial.print("Spread: "); Serial.print(spread); Serial.println("C");
  Serial.println("----------------------------------------");

  delay(2000); 
}