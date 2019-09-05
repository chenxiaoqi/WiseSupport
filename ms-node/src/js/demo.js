import chai from 'chai';
const expect = chai.expect;

const map = new Map([['a',1]]);
expect(map.get('a')).to.equal(1);

map.forEach(function (key, value) {
    console.log(key + "=" + value);
});