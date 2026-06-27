package com.mojang.rubydung.character;

import org.lwjgl.opengl.GL11;

public class Polygon {
  public Vertex[] vertices;
  
  public int vertexCount;
  
  public Polygon(Vertex[] vertices) {
    this.vertices = vertices;
    this.vertexCount = vertices.length;
  }
  
  public Polygon(Vertex[] vertices, int minU, int minV, int maxU, int maxV) {
    this(vertices);
    vertices[0] = vertices[0].remap(maxU, minV);
    vertices[1] = vertices[1].remap(minU, minV);
    vertices[2] = vertices[2].remap(minU, maxV);
    vertices[3] = vertices[3].remap(maxU, maxV);
  }
  
  public void render() {
    GL11.glColor3f(1.0F, 1.0F, 1.0F);
    for (int i = 3; i >= 0; i--) {
      Vertex vertex = this.vertices[i];
      GL11.glTexCoord2f(vertex.u / 64.0F, vertex.v / 32.0F);
      GL11.glVertex3f(vertex.position.x, vertex.position.y, vertex.position.z);
    } 
  }
}
