import React, {Component} from 'react';
import { StyleSheet, Text, View, TextInput, TouchableOpacity, Alert, Image, KeyboardAvoidingView} from 'react-native';
import logo from "./assests/logo.png";
import * as Firebase from "firebase"
//var firebase = require("firebase");



export default class App extends Component {
  constructor(props){
    super(props);
    this.state = {
      name: 'text',
      pass:'text'
    };
  }

  login = () => {
    Firebase.auth().signInWithEmailAndPassword(this.state.name, this.state.pass)
  .then((user) => {
    // If you need to do anything with the user, do it here
    // The user will be logged in automatically by the
    // `onAuthStateChanged` listener we set up in App.js earlier
    alert('logged in');
    console.log('Logged in');
  })
  .catch((error) => {
    // For details of error codes, see the docs
    // The message contains the default Firebase string
    // representation of the error
    console.log('didn\'t log in');
    alert('Failed to login. Please try again.');

  });

  }
  createacc = () => {
    Firebase.auth().createUserWithEmailAndPassword(this.state.name, this.state.pass)
  .then((user) => {
    // If you need to do anything with the user, do it here
    // The user will be logged in automatically by the
    // `onAuthStateChanged` listener we set up in App.js earlier
    console.log('Created account');
  })
  .catch((error) => {
    const { code, message } = error;
    // For details of error codes, see the docs
    // The message contains the default Firebase string
    // representation of the error
    console.log('failed to create account');
    alert('Failed to create account')
  });
  }
  forgotpassword = () => {
    Alert.alert("To be implemented")
  }

  render() {
    return (
      <KeyboardAvoidingView behavior="padding"style={styles.container}>
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
      keyboardType="email-address"
      placeholder='Username'
      onChangeText={
        (text) =>{
          this.setState(
            (previousState) => {
              return {
                name: text
              };

            });
          }
        }
        />
        <TextInput style={{
          height:40,
          margin:10,
          padding:10,
          borderColor: 'gray',
          borderWidth:1,
        } }
        placeholder='Password'
        secureTextEntry={true}
        onChangeText={(text) => {this.setState(() => {
          return {
            pass: text
          }
        })}}
        />
        <TouchableOpacity style = {styles.loginbtn}
        onPress={this.login}
        >
        <Text style={{
          fontSize: 20,
          color: 'white',
        }}> Login
        </Text>
        </TouchableOpacity>
        <TouchableOpacity style = {styles.btncreate}
       onPress={this.createacc}
       >
        <Text style={{
          fontSize: 20,
          paddingTop: 20,
          alignSelf: 'center',
        }}> Create an account
        </Text>
        </TouchableOpacity>
        <TouchableOpacity style = {styles.btn}
       onPress={this.forgotpassword}
       >
        <Text style={{
          fontSize: 20,
          paddingTop: 20,
          alignSelf: 'center',
        }}> Forgot Password
        </Text>
        </TouchableOpacity>
        </KeyboardAvoidingView>
      );
    }
  }

  var config = {
  apiKey: "AIzaSyA8E94qwPa4Um0UMoG83rOJ_izZVQt-lGE",
  authDomain: "personalassistant-ec554.firebaseapp.com",
  databaseURL: "https://personalassistant-ec554.firebaseio.com",
  storageBucket: "personalassistant-ec554.appspot.com.appspot.com",
};
Firebase.initializeApp(config);

  const styles = StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: '#fff',
      justifyContent: 'center',
    },
    loginbtn: {
      height: 40,
      width: 200,
      backgroundColor: '#3373f4',
      padding: 20,
      alignSelf: 'center',
      alignItems: 'center',
      justifyContent: 'center',
    },
    btn: {
      height: 40,
      width: 200,
      padding: 20,
      alignSelf: 'center',
      alignItems: 'center',
      justifyContent: 'center',
    },
    btncreate: {
      height: 40,
      width: 250,
      padding: 20,
      alignSelf: 'center',
      alignItems: 'center',
      justifyContent: 'center',
      
    }

  });
