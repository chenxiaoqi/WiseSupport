"use strict"
import chai from 'chai';

const expect = chai.expect;

(function () {
    const map = new Map([['a', 1]]);
    expect(map.get('a')).to.equal(1);
    map.forEach(function (key, value) {
        console.log(`${key}=${value}`);
    });
})();


(function () {
    const fruits = ['apple', 'orange'];

    fruits.push('strawberry');
    expect(fruits).deep.equal(['apple', 'orange', 'strawberry']);
    fruits.pop();
    expect(fruits).deep.equal(['apple', 'orange']);

    fruits.shift();
    expect(fruits).deep.equal(['orange']);
    fruits.unshift('apple');
    expect(fruits).deep.equal(['apple', 'orange']);

    expect(fruits.indexOf('apple')).to.equal(0);

    const arr = [0, 1, 2, 3, 4, 5, 6];
    let removed = arr.splice(2, 2);
    expect(arr).deep.equal([0, 1, 4, 5, 6]);
    expect(removed).deep.equal([2, 3]);


})();

