package com.mojang.rubydung.character;

public class Vec3 {
  public float x;
  
  public float y;
  
  public float z;
  
  public Vec3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public Vec3 interpolateTo(Vec3 vector, float partialTicks) {
    float interpolatedX = this.x + (vector.x - this.x) * partialTicks;
    float interpolatedY = this.y + (vector.y - this.y) * partialTicks;
    float interpolatedZ = this.z + (vector.z - this.z) * partialTicks;
    return new Vec3(interpolatedX, interpolatedY, interpolatedZ);
  }
  
  public void set(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
