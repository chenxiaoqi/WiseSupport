(function () {

    let obj = {};
    Object.defineProperties(obj, {
        p1: {
            // value: 'i am pi',
            enumerable: false,
            // writable: false,
            get() {
                return 'haha';
            },
            set(v) {

            }

        }
    });

    obj.p1 = 'error';
    console.log(obj.p1);

    // console.log(obj.p1);

    function Point() {
        this.x = 1;
        this.y = 2;
    }

    // Point.prototype = {_x: 9, _y: 9};
    Object.assign(Point.prototype, {_x: 9, _y: 9});
    let point = new Point();
    console.log(point);
    console.log(point._x, point._y, point.x, point.y);

    function Shape() {
        Point.call(this);
    }

    Shape.prototype = Object.create(Point.prototype);
    Shape.prototype.constructor = Shape;
    let shape = new Shape();
    console.log(point.constructor);
    console.log(shape.constructor);
    console.log(shape instanceof Shape);
    console.log(shape instanceof Point);


    function Person(age, name) {
        this.age = age;
        this.name = name;
    }

    Person.prototype.email = 'cxqpfan@163.com';
    Person.prototype.say = function () {
        return `hello ${this.name}`;
    };

    let person = new Person(18, 'Mike');
    person.gender = 'female';
    console.log(`person owner property age ${person.hasOwnProperty('age')}`);
    console.log(`person owner property name ${person.hasOwnProperty('name')}`);
    console.log(`person owner property gender ${person.hasOwnProperty('gender')}`);
    console.log(`person owner property email ${person.hasOwnProperty('email')}`);
    console.log(`person keys ${Object.keys(person)}`);
    let keys = [];
    for (let k in person) {
        keys.push(k);
        console.log(`typeof ${k} is ${typeof (person[k])}`)
    }
    console.log(`for each keys ${keys}`);


})();
