import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Get Battery Level'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  late StreamSubscription _streamSubscription;

  String batteryLevel = "Waiting...";
  String batteryEventStatus = "Streaming...";

  static const batteryChannel = MethodChannel("com.example.flutter_native_connect_practice/battery");
  static const batteryEvent = EventChannel("com.example.flutter_native_connect_practice/charging");

  @override
  void initState() {
    super.initState();
    onListenBattery();
    onStreamBattery();
  }

  @override
  void dispose() {
    _streamSubscription.cancel();
    super.dispose();
  }

  void onStreamBattery() {
    _streamSubscription = batteryEvent.receiveBroadcastStream().listen((event) {
      setState(() {
        batteryEventStatus = "$event";
      });
    });
  }

  void onListenBattery() {
    batteryChannel.setMethodCallHandler((call) async {
      if(call.method == "reportBatteryLevel"){
       final int batteryLe = call.arguments;
       setState(() {
         batteryLevel = "$batteryLe";
       });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              "Battery Level"
            ),
            Text(
              batteryLevel,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const Text(
                "Battery Charging Status"
            ),
            Text(
              batteryEventStatus,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            // ElevatedButton(
            //     onPressed: getBatteryLevel,
            //     child: const Text("Get Battery Level")
            // )
          ],
        ),
      ),
    );
  }

  Future getBatteryLevel() async {
    final arguments = {
      'name':"Sujith B"
    };
    final String newBatteryLevel = await batteryChannel.invokeMethod('getBatteryLevel', arguments);

    setState(() {
      batteryLevel = '$newBatteryLevel';
    });
  }


}
