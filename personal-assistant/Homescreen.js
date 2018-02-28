import React, {Component} from 'react';
import { StyleSheet, Text, View, TextInput, TouchableOpacity, Alert, Image } from 'react-native';
import logo from "./assests/logo.png";

export default class App extends Component {
  constructor(props){
    super(props);
    this.state = {
      name: 'text',
      pass:'text'
    };
  }
  press = () => {
    Alert.alert("You have logged in")
  }
  createacc = () => {
    Alert.alert("To be implemented")
  }
  forgotpassword = () => {
    Alert.alert("To be implemented")
  }

  render() {
    return (
      <View>
      <Text> Text </Text>
      <Image style= {{
        alignSelf: 'center',
        justifyContent: 'center',
        marginTop: 70,
      }}
      source = {logo} />
      <TextInput style={{
        height:40,
        margin:10,
        marginTop: 50,
        padding:10,
        borderColor: 'gray',
        borderWidth:1,
      } }
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  btn: {
    height: 40,
    width: 200,
    backgroundColor: '#3373f4',
    padding: 20,
    alignSelf: 'center',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
