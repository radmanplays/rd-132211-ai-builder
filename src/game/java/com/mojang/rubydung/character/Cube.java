package com.mojang.rubydung.character;

import org.lwjgl.opengl.GL11;

public class Cube {
  private Polygon[] polygons;
  
  private int textureOffsetX;
  
  private int textureOffsetY;
  
  public float x;
  
  public float y;
  
  public float z;
  
  public float xRotation;
  
  public float yRotation;
  
  public float zRotation;
  
  public Cube(int textureOffsetX, int textureOffsetY) {
    this.textureOffsetX = textureOffsetX;
    this.textureOffsetY = textureOffsetY;
  }
  
  public void setTextureOffset(int textureOffsetX, int textureOffsetY) {
    this.textureOffsetX = textureOffsetX;
    this.textureOffsetY = textureOffsetY;
  }
  
  public Cube addBox(float offsetX, float offsetY, float offsetZ, int width, int height, int depth) {
    this.polygons = new Polygon[6];
    float x = offsetX + width;
    float y = offsetY + height;
    float z = offsetZ + depth;
    Vertex vertexBottom1 = new Vertex(offsetX, offsetY, offsetZ, 0.0F, 0.0F);
    Vertex vertexBottom2 = new Vertex(x, offsetY, offsetZ, 0.0F, 8.0F);
    Vertex vertexBottom3 = new Vertex(offsetX, offsetY, z, 0.0F, 0.0F);
    Vertex vertexBottom4 = new Vertex(x, offsetY, z, 0.0F, 8.0F);
    Vertex vertexTop1 = new Vertex(x, y, z, 8.0F, 8.0F);
    Vertex vertexTop2 = new Vertex(offsetX, y, z, 8.0F, 0.0F);
    Vertex vertexTop3 = new Vertex(x, y, offsetZ, 8.0F, 8.0F);
    Vertex vertexTop4 = new Vertex(offsetX, y, offsetZ, 8.0F, 0.0F);
    this.polygons[0] = new Polygon(new Vertex[] { vertexBottom4, vertexBottom2, vertexTop3, vertexTop1 }, this.textureOffsetX + depth + width, this.textureOffsetY + depth, this.textureOffsetX + depth + width + depth, this.textureOffsetY + depth + height);
    this.polygons[1] = new Polygon(new Vertex[] { vertexBottom1, vertexBottom3, vertexTop2, vertexTop4 }, this.textureOffsetX, this.textureOffsetY + depth, this.textureOffsetX + depth, this.textureOffsetY + depth + height);
    this.polygons[2] = new Polygon(new Vertex[] { vertexBottom4, vertexBottom3, vertexBottom1, vertexBottom2 }, this.textureOffsetX + depth, this.textureOffsetY, this.textureOffsetX + depth + width, this.textureOffsetY + depth);
    this.polygons[3] = new Polygon(new Vertex[] { vertexTop3, vertexTop4, vertexTop2, vertexTop1 }, this.textureOffsetX + depth + width, this.textureOffsetY, this.textureOffsetX + depth + width + width, this.textureOffsetY + depth);
    this.polygons[4] = new Polygon(new Vertex[] { vertexBottom2, vertexBottom1, vertexTop4, vertexTop3 }, this.textureOffsetX + depth, this.textureOffsetY + depth, this.textureOffsetX + depth + width, this.textureOffsetY + depth + height);
    this.polygons[5] = new Polygon(new Vertex[] { vertexBottom3, vertexBottom4, vertexTop1, vertexTop2 }, this.textureOffsetX + depth + width + depth, this.textureOffsetY + depth, this.textureOffsetX + depth + width + depth + width, this.textureOffsetY + depth + height);
    return this;
  }
  
  public void setPosition(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  public void render() {
    GL11.glPushMatrix();
    GL11.glTranslatef(this.x, this.y, this.z);
    GL11.glRotatef((float)Math.toDegrees(this.zRotation), (float)0.0D,(float) 0.0D, (float) 1.0D);
    GL11.glRotatef((float) Math.toDegrees(this.yRotation), (float) 0.0D, (float) 1.0D, (float) 0.0D);
    GL11.glRotatef((float) Math.toDegrees(this.xRotation), (float) 1.0D, (float) 0.0D, (float) 0.0D);
    GL11.glBegin(7);
    for (Polygon polygon : this.polygons)
      polygon.render(); 
    GL11.glEnd();
    GL11.glPopMatrix();
  }
}
