from flask import jsonify

def api_response(code, data=None, message=None):
    return jsonify({
        'code': code,
        'data': data,
        'message': message
    }), code