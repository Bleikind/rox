Array.prototype.shuffle = function() {
    var i = this.length, j, temp;
    if (i > 1) while (--i) {j = Math.floor(Math.random() * (i + 1)); temp = this[i]; this[i] = this[j]; this[j] = temp; }
    return this;
};