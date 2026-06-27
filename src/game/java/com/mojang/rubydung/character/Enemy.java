package com.mojang.rubydung.character;

import com.mojang.rubydung.Entity;
import com.mojang.rubydung.Textures;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.ml.AIManager;
import com.mojang.rubydung.phys.AABB;
import org.lwjgl.opengl.GL11;

public class Enemy extends Entity {
  public Cube head;
  
  public Cube body;
  
  public Cube rightArm;
  
  public Cube leftArm;
  
  public Cube rightLeg;
  
  public Cube leftLeg;
  
  public double rotation = Math.random() * Math.PI * 2.0D;
  
  public double rotationMotionFactor = (Math.random() + 1.0D) * 0.009999999776482582D;
  
  public float timeOffset = (float)(Math.random() * 1239813.0D);
  
  public float speed = 1.0F;
  
  private boolean isStatic = false;
  
  private static int characterTexture = -1;
  
  public Enemy(Level level, double x, double y, double z) {
    super(level);
    if (characterTexture == -1)
      characterTexture = Textures.loadTexture("/char.png", 9728); 
    this.x = x;
    this.y = y;
    this.z = z;
    this.prevX = x;
    this.prevY = y;
    this.prevZ = z;
    float width = 0.6F;
    float height = 1.8F;
    this.boundingBox = new AABB(x - (width / 2.0F), y - (height / 2.0F), z - (width / 2.0F), x + (width / 2.0F), y + (height / 2.0F), z + (width / 2.0F));
    this.heightOffset = 0.0F;
    this
      .head = (new Cube(0, 0)).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8);
    this
      .body = (new Cube(16, 16)).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4);
    this
      .rightArm = (new Cube(40, 16)).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
    this.rightArm.setPosition(-5.0F, 2.0F, 0.0F);
    this
      .leftArm = (new Cube(40, 16)).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
    this.leftArm.setPosition(5.0F, 2.0F, 0.0F);
    this
      .rightLeg = (new Cube(0, 16)).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
    this.rightLeg.setPosition(-2.0F, 12.0F, 0.0F);
    this
      .leftLeg = (new Cube(0, 16)).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
    this.leftLeg.setPosition(2.0F, 12.0F, 0.0F);
    if (this.isAIBuilder) {
      AIManager aiManager = new AIManager();
      setAIManager(aiManager);
      System.out.println("Enemy AI Builder initialized with ML!");
    } 
  }
  
  public void tick() {
    super.tick();
    if (this.isStatic && this.isAIBuilder)
      return; 
    if (this.isStatic)
      return; 
    this.rotation += this.rotationMotionFactor;
    this.rotationMotionFactor *= 0.99D;
    this.rotationMotionFactor += (Math.random() - Math.random()) * Math.random() * Math.random() * 0.009999999776482582D;
    float vertical = (float)Math.sin(this.rotation);
    float forward = (float)Math.cos(this.rotation);
    if (this.onGround && Math.random() < 0.01D)
      this.motionY = 0.11999999731779099D; 
    moveRelative(vertical, forward, this.onGround ? 0.02F : 0.005F);
    this.motionY -= 0.004999999888241291D;
    move(this.motionX, this.motionY, this.motionZ);
    this.motionX *= 0.9100000262260437D;
    this.motionY *= 0.9800000190734863D;
    this.motionZ *= 0.9100000262260437D;
    if (this.y < -100.0D)
      resetPosition(); 
    if (this.onGround) {
      this.motionX *= 0.800000011920929D;
      this.motionZ *= 0.800000011920929D;
    } 
  }
  
  public void render(float partialTicks) {
    double offsetY;
    GL11.glPushMatrix();
    GL11.glEnable(3553);
    GL11.glBindTexture(3553, characterTexture);
    double time = System.nanoTime() / 1.0E9D * 10.0D * this.speed + this.timeOffset;
    double interpolatedX = this.prevX + (this.x - this.prevX) * partialTicks;
    double interpolatedY = this.prevY + (this.y - this.prevY) * partialTicks;
    double interpolatedZ = this.prevZ + (this.z - this.prevZ) * partialTicks;
    GL11.glTranslatef((float) interpolatedX, (float) interpolatedY, (float) interpolatedZ);
    GL11.glScalef(1.0F, -1.0F, 1.0F);
    float size = 0.058333334F;
    GL11.glScalef(size, size, size);
    if (this.isStatic && !this.isAIBuilder) {
      offsetY = 23.0D;
    } else {
      offsetY = Math.abs(Math.sin(time * 2.0D / 3.0D)) * 5.0D + 23.0D;
    } 
    GL11.glTranslatef((float)0.0D, (float) -offsetY, (float) 0.0D);
    if (!this.isStatic || this.isAIBuilder)
      if (this.isAIBuilder) {
        GL11.glRotatef((float) Math.toDegrees(this.yRotation) + (float) 180.0D, (float) 0.0D, (float) 1.0D, (float) 0.0D);
      } else {
        GL11.glRotatef((float) Math.toDegrees(this.rotation) + (float) 180.0D, (float) 0.0D, (float) 1.0D, (float) 0.0D);
      }  
    if (!this.isStatic || this.isAIBuilder) {
      this.head.yRotation = (float)Math.sin(time * 0.83D);
      this.head.xRotation = (float)Math.sin(time) * 0.8F;
      this.rightArm.xRotation = (float)Math.sin(time * 0.6662D + Math.PI) * 2.0F;
      this.rightArm.zRotation = (float)(Math.sin(time * 0.2312D) + 1.0D);
      this.leftArm.xRotation = (float)Math.sin(time * 0.6662D) * 2.0F;
      this.leftArm.zRotation = (float)(Math.sin(time * 0.2812D) - 1.0D);
      this.rightLeg.xRotation = (float)Math.sin(time * 0.6662D) * 1.4F;
      this.leftLeg.xRotation = (float)Math.sin(time * 0.6662D + Math.PI) * 1.4F;
    } else {
      this.head.yRotation = 0.0F;
      this.head.xRotation = 0.0F;
      this.rightArm.xRotation = 0.0F;
      this.rightArm.zRotation = 0.0F;
      this.leftArm.xRotation = 0.0F;
      this.leftArm.zRotation = 0.0F;
      this.rightLeg.xRotation = 0.0F;
      this.leftLeg.xRotation = 0.0F;
    } 
    this.head.render();
    this.body.render();
    this.rightArm.render();
    this.leftArm.render();
    this.rightLeg.render();
    this.leftLeg.render();
    GL11.glDisable(3553);
    GL11.glPopMatrix();
  }
  
  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }
  
  public boolean isStatic() {
    return this.isStatic;
  }
  
  public void enableAIBuilder() {
    this.isAIBuilder = true;
    setStatic(true);
    System.out.println("Enemy enabled as AI Builder");
  }
  
  public boolean isAIBuilder() {
    return this.isAIBuilder;
  }
  
  public String getAIStatus() {
    return super.getAIStatus();
  }
}
