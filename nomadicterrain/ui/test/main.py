from flask import Flask, render_template

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('/index.html')

@app.route('/location/<coordinates>')
def location(coordinates):
    return render_template('/location.html', location=coordinates)

if __name__ == '__main__':
    app.debug = True
    app.run(host="localhost", port=5000)
