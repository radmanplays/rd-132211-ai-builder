package com.mojang.rubydung.character;

public class Vertex {
  public Vec3 position;
  
  public float u;
  
  public float v;
  
  public Vertex(float x, float y, float z, float u, float v) {
    this(new Vec3(x, y, z), u, v);
  }
  
  public Vertex(Vertex vertex, float u, float v) {
    this.position = vertex.position;
    this.u = u;
    this.v = v;
  }
  
  public Vertex(Vec3 position, float u, float v) {
    this.position = position;
    this.u = u;
    this.v = v;
  }
  
  public Vertex remap(float u, float v) {
    return new Vertex(this, u, v);
  }
}
