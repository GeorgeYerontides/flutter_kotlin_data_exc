import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

const platform = const MethodChannel('channel_test');

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Module',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Module'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  String _receivedMessage = '';

  @override
  void initState() {
    super.initState();

    // Listen for messages from the Android side
    platform.setMethodCallHandler((call) async {
      if (call.method == 'sendDataToFlutter') {
        setState(() {
          print(call.arguments);
          _receivedMessage = call.arguments['data'] ?? ''; // Provide a default value if null
          print('Received data from Android: $_receivedMessage'); // Print the data
        });
      }
      // Always return a non-null value
      return null;
    });
  }

  void _incrementCounter() {
    setState(() {
      _counter++;
      // Send counter value back to Android
      platform.invokeMethod('updateCounter', {'counter': _counter});
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _receivedMessage.isNotEmpty ? _receivedMessage : 'No message',
              style: TextStyle(fontSize: 18),
            ),
            const SizedBox(height: 20),
            const Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }
}
