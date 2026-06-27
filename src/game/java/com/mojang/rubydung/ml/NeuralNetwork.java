package com.mojang.rubydung.ml;

import com.mojang.rubydung.DebugLogger;
import java.util.Random;

public class NeuralNetwork {
  private double[][] weights;
  
  private double[][] bias;
  
  private double learningRate = 0.01D;
  
  private Random random = new Random();
  
  public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
    this.weights = new double[hiddenSize][inputSize];
    this.bias = new double[outputSize][hiddenSize];
    int i;
    for (i = 0; i < hiddenSize; i++) {
      for (int j = 0; j < inputSize; j++)
        this.weights[i][j] = this.random.nextDouble() * 2.0D - 1.0D; 
    } 
    for (i = 0; i < outputSize; i++) {
      for (int j = 0; j < hiddenSize; j++)
        this.bias[i][j] = this.random.nextDouble() * 2.0D - 1.0D; 
    } 
  }
  
  public double[] predict(double[] inputs) {
    double[] hidden = new double[this.weights.length];
    for (int i = 0; i < hidden.length; i++) {
      double sum = 0.0D;
      for (int k = 0; k < inputs.length; k++)
        sum += this.weights[i][k] * inputs[k]; 
      hidden[i] = Math.tanh(sum);
    } 
    double[] outputs = new double[this.bias.length];
    for (int j = 0; j < outputs.length; j++) {
      double sum = 0.0D;
      for (int k = 0; k < hidden.length; k++)
        sum += this.bias[j][k] * hidden[k]; 
      outputs[j] = Math.tanh(sum);
    } 
    return outputs;
  }
  
  public void train(double[] inputs, double[] targets) {
    double[] outputs = predict(inputs);
    double[] outputErrors = new double[outputs.length];
    for (int i = 0; i < outputErrors.length; i++)
      outputErrors[i] = targets[i] - outputs[i]; 
    double[] hidden = new double[this.weights.length];
    int j;
    for (j = 0; j < hidden.length; j++) {
      double sum = 0.0D;
      for (int k = 0; k < inputs.length; k++)
        sum += this.weights[j][k] * inputs[k]; 
      hidden[j] = Math.tanh(sum);
    } 
    for (j = 0; j < this.bias.length; j++) {
      for (int k = 0; k < hidden.length; k++)
        this.bias[j][k] = this.bias[j][k] + this.learningRate * outputErrors[j] * hidden[k]; 
    } 
    for (j = 0; j < this.weights.length; j++) {
      double hiddenError = 0.0D;
      for (int k = 0; k < outputErrors.length; k++)
        hiddenError += outputErrors[k] * this.bias[k][j]; 
      for (int m = 0; m < inputs.length; m++)
        this.weights[j][m] = this.weights[j][m] + this.learningRate * hiddenError * inputs[m] * (1.0D - hidden[j] * hidden[j]); 
    } 
  }
  
  public void save(String filename) {
    DebugLogger.ml("Saving neural network to " + filename);
  }
  
  public void load(String filename) {
    DebugLogger.ml("Loading neural network from " + filename);
  }
}
