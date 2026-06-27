package com.mojang.rubydung.ml;

import com.mojang.rubydung.DebugLogger;
import java.util.ArrayList;
import java.util.List;

public class TrainingData {
  private List<Experience> experiences = new ArrayList<>();
  
  public static class Experience {
    public double[] inputs;
    
    public double[] outputs;
    
    public double reward;
    
    public Experience(double[] inputs, double[] outputs, double reward) {
      this.inputs = inputs;
      this.outputs = outputs;
      this.reward = reward;
    }
  }
  
  public void addExperience(double[] inputs, double[] outputs, double reward) {
    this.experiences.add(new Experience(inputs, outputs, reward));
  }
  
  public List<Experience> getExperiences() {
    return this.experiences;
  }
  
  public void clear() {
    this.experiences.clear();
  }
  
  public double[][] getInputs() {
    double[][] result = new double[this.experiences.size()][];
    for (int i = 0; i < this.experiences.size(); i++)
      result[i] = ((Experience)this.experiences.get(i)).inputs; 
    return result;
  }
  
  public double[][] getOutputs() {
    double[][] result = new double[this.experiences.size()][];
    for (int i = 0; i < this.experiences.size(); i++)
      result[i] = ((Experience)this.experiences.get(i)).outputs; 
    return result;
  }
  
  public void save(String filename) {
    DebugLogger.ml("Saving " + this.experiences.size() + " training examples to " + filename);
  }
  
  public void load(String filename) {
    DebugLogger.ml("Loading training data from " + filename);
  }
}
