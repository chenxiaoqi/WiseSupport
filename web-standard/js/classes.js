(function () {
    const prefix = 'prefixed';

    class Point {
        static static_field = 'i am static field';
        [`${prefix}Field`] = 'prefixed field';
        x = 1;
        y = 1;

        constructor(x, y) {
            this.x = x;
            this.y = y;
        }

        move() {
            console.log(`move ${this.x} ${this.y}`)
        }
    }

    class Line extends Point {
        _msg = 'hello';
        get msg() {
            return this._msg;
        }

        set msg(v) {
            this._msg = `hello ${v}`
        }
    }

    let point = new Point(2, 2);
    console.log(point.prefixedField);
    point.move();

    let line = new Line();
    line.msg = 'cxq';
    console.log(line.msg);
    console.log('Object.keys()', Object.keys(line))

})();