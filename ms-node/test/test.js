const assert = require('assert');
const chai = require('chai');
let expect = chai.expect;
describe('Array()', function () {
    describe('#indexOf()', function () {
        it('should return -1 when the value is not present', function () {
            assert.strictEqual([1, 2, 3].indexOf(0), -1, 'xxxxx');
        });
    });
});

describe('Built-in Properties', function (ttt) {
    it('should success', function () {
        assert.strictEqual(isFinite(Infinity), false);
        assert.strictEqual(ttt === undefined, true);
    });
});

describe('Built-in Functions', function () {
    it('should success', function () {
        assert.strictEqual(isFinite(Infinity), false);
        assert.strictEqual(isNaN(Infinity), false);
        assert.strictEqual(isNaN('ha'), true);
        assert.strictEqual(isNaN('1'), false);
        assert.strictEqual(isNaN(1), false);
    });

    it('encodeURI decodeURI', function () {
        assert.strictEqual(encodeURI(':='), ':=');
        assert.notStrictEqual(encodeURIComponent(':='), ':=');
    })
});

describe('Object', function () {
    it('object basic', function () {
        assert.strictEqual(typeof Object, 'function')
    });

    it('#prototype', function () {
        Object.prototype.testmyproperty = 'fun';
        assert.strictEqual({}.testmyproperty, 'fun');

        function Apple() {
        }

        Apple.prototype.c = 'c';
        assert.strictEqual(new Apple().c, 'c');
        Apple.prototype = {a: 1, b: 2};
        assert.strictEqual(new Apple().a, 1);
        assert.strictEqual(new Apple().c, undefined);


    });

    it('#asign()', function () {
        var target = {a: 1, b: 3};
        var source = {b: 4, c: 5};
        var result = Object.assign(target, source);
        assert.strictEqual(target, result);
        assert.strictEqual(target === result, true);
        assert.notStrictEqual(target, {a: 1, b: 4, c: 5});
        assert.deepStrictEqual(target, {a: 1, b: 4, c: 5});
    });

    it("#create() property is from prototype chain", function () {
        var b = Object.create({a: 1});
        expect(b).to.not.have.keys('a');
        expect(b).to.have.property('a');
    });

    it("#defineProperty() #defineProperties()", function () {
        var a = {};
        Object.defineProperty(a, 'pro1', {value: 42, writable: false});
        expect(a).to.not.have.keys('pro1');
        expect(a).to.have.property('pro1');
        a.pro1 = 3;
        expect(a.pro1).to.not.equal(3);

        Object.defineProperties(a, {pro2: {value: 2, writable: false}, pro3: {value: 3, writable: true}});
        expect(a).to.have.property('pro2', 2);
        expect(a).to.have.property('pro3', 3);
        a.pro3 = 42;
        expect(a).to.have.property('pro3', 42);
        expect(a).is.a('object');
    });

    it("#entries() keys() values()", function () {
        const a = {
            a: 'somestring',
            b: 42
        };

        expect(a).to.have.property('testmyproperty', 'fun');

        expect(Object.entries(a)).to.have.lengthOf(2);
        for (let [key, value] of Object.entries(a)) {
            expect(key).to.be.oneOf(['a', 'b']);
            expect(value).to.be.oneOf(['somestring', 42]);
        }

        expect(Object.keys(a)).to.have.lengthOf(2);
        expect(Object.values(a)).to.have.lengthOf(2);

        Object.keys(a).forEach((v)=>console.log(v))

    });



});
