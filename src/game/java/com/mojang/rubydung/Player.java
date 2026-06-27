package com.mojang.rubydung;

import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.phys.AABB;
import java.util.List;
import org.lwjgl.input.Keyboard;

public class Player {
  private final Level level;
  
  public double x;
  
  public double y;
  
  public double z;
  
  public double prevX;
  
  public double prevY;
  
  public double prevZ;
  
  public double motionX;
  
  public double motionY;
  
  public double motionZ;
  
  public float xRotation;
  
  public float yRotation;
  
  private boolean onGround;
  
  public AABB boundingBox;
  
  public Player(Level level) {
    this.level = level;
    resetPosition();
  }
  
  private void setPosition(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    float width = 0.3F;
    float height = 0.9F;
    this.boundingBox = new AABB((x - width), (y - height), (z - width), (x + width), (y + height), (z + width));
  }
  
  void resetPosition() {
    float x = (float)Math.random() * this.level.width;
    float y = (this.level.depth + 3);
    float z = (float)Math.random() * this.level.height;
    setPosition(x, y, z);
  }
  
  public void turn(float x, float y) {
    this.yRotation += x * 0.15F;
    this.xRotation -= y * 0.15F;
    this.xRotation = Math.max(-90.0F, this.xRotation);
    this.xRotation = Math.min(90.0F, this.xRotation);
  }
  
  public void tick() {
    this.prevX = this.x;
    this.prevY = this.y;
    this.prevZ = this.z;
    float forward = 0.0F;
    float vertical = 0.0F;
    if (Keyboard.isKeyDown(19))
      resetPosition(); 
    if (Keyboard.isKeyDown(200) || Keyboard.isKeyDown(17))
      forward--; 
    if (Keyboard.isKeyDown(208) || Keyboard.isKeyDown(31))
      forward++; 
    if (Keyboard.isKeyDown(203) || Keyboard.isKeyDown(30))
      vertical--; 
    if (Keyboard.isKeyDown(205) || Keyboard.isKeyDown(32))
      vertical++; 
    if ((Keyboard.isKeyDown(57)) && this.onGround)
      this.motionY = 0.11999999731779099D; 
    boolean isSprinting = Keyboard.isKeyDown(42);
    float speedMultiplier = isSprinting ? 1.8F : 1.0F;
    moveRelative(vertical, forward, (this.onGround ? 0.02F : 0.005F) * speedMultiplier);
    this.motionY -= 0.005D;
    move(this.motionX, this.motionY, this.motionZ);
    float friction = isSprinting ? 0.95F : 0.91F;
    this.motionX *= friction;
    this.motionY *= 0.9800000190734863D;
    this.motionZ *= friction;
    if (this.onGround) {
      float groundFriction = isSprinting ? 0.85F : 0.8F;
      this.motionX *= groundFriction;
      this.motionZ *= groundFriction;
    } 
  }
  
  public void move(double x, double y, double z) {
    double prevX = x;
    double prevY = y;
    double prevZ = z;
    List<AABB> aABBs = this.level.getCubes(this.boundingBox.expand(x, y, z));
    for (AABB abb : aABBs)
      y = abb.clipYCollide(this.boundingBox, y); 
    this.boundingBox.move(0.0D, y, 0.0D);
    for (AABB aABB : aABBs)
      x = aABB.clipXCollide(this.boundingBox, x); 
    this.boundingBox.move(x, 0.0D, 0.0D);
    for (AABB aABB : aABBs)
      z = aABB.clipZCollide(this.boundingBox, z); 
    this.boundingBox.move(0.0D, 0.0D, z);
    this.onGround = (prevY != y && prevY < 0.0D);
    if (prevX != x)
      this.motionX = 0.0D; 
    if (prevY != y)
      this.motionY = 0.0D; 
    if (prevZ != z)
      this.motionZ = 0.0D; 
    this.x = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
    this.y = this.boundingBox.minY + 1.62D;
    this.z = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
  }
  
  private void moveRelative(float x, float z, float speed) {
    float distance = x * x + z * z;
    if (distance < 0.01F)
      return; 
    distance = speed / (float)Math.sqrt(distance);
    x *= distance;
    z *= distance;
    double sin = Math.sin(Math.toRadians(this.yRotation));
    double cos = Math.cos(Math.toRadians(this.yRotation));
    this.motionX += x * cos - z * sin;
    this.motionZ += z * cos + x * sin;
  }
}
