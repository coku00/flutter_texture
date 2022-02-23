import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

MethodChannel methodChannel = MethodChannel('flutter/texture/channel');

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  int textureId = -1;

  Future<void> _createTexture() async {
    print('textureId = $textureId');
    if (textureId < 0) {
      methodChannel.invokeMethod('createTexture').then((value) {
        textureId = value;
        setState(() {
          print('textureId ==== $textureId');
        });
      });
    }

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
            if (textureId > -1)
              Container(
                width: 300,
                height: 400,
                child: Texture(
                  textureId: textureId,
                ),
              )
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _createTexture,
        tooltip: 'createTexture',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
