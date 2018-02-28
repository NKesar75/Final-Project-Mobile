import react, {Component} from 'react';
import {
  platfrom,
  StyleSheet,
  Text,
  View
} from 'react-native';
import { StackNavigator } from 'react-navigation';
import login from './App';
import Homescreen from './Homescreen';

const Screens = StackNavigator({
  login: {screen:login},
  homescreen: {screen: Homescreen},
});
